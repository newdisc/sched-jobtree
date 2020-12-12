package nd.sched.trigger;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nd.sched.job.JobReturn;
import nd.sched.job.JobReturn.JobStatus;
import nd.sched.job.service.IJobExecutorService;

public class TriggerService {
	private static final Logger logger = LoggerFactory.getLogger(TriggerService.class);
	private TriggerTreeCache triggersCache;
	private TriggerConditionChecker conditionChecker;
	private Map<String, IJobExecutorService> jobExecutors = new TreeMap<>(); 
	public boolean isTriggerConditionsOK(final String triggerName) {
		logger.debug("Checking trigger: {}", triggerName);
		final Trigger tocheck = triggersCache.get(triggerName);
		if (null == tocheck) {
			logger.error("Could NOT find trigger: {}", triggerName);
			return false;
		}
		return conditionChecker.isTriggerConditionsOK(tocheck);
	}
	public void runAll() {
		triggersCache.keySet().stream().filter(this::isTriggerConditionsOK).map(triggersCache::get)
			.forEach(trig -> {
				logger.info("Running Trigger: {}", trig.getName());
				initiateDependents(trig);
		});
	}
	public void initiateDependents(final Trigger trigger) {
		trigger.getInterested().stream().filter(conditionChecker::isTriggerConditionsOK).forEach(runTrig -> {
			logger.info("Initiating Run: {}", runTrig.getName());
			final IJobExecutorService jes = jobExecutors.get(runTrig.getQualifier());
			jes.initiateExecute(runTrig.getName(), runTrig.getJob(), 
					runTrig.getArguments(), jr -> jobCallback(runTrig, jr));
		});
		final Trigger parent = triggersCache.get(trigger.getParent());
		if (null == parent) { // top level box
			return;
		}
		final boolean parentComplete = parent.getChildren().stream().allMatch(
				child -> ((TriggerStatus.SUCCESS == child.getStatus()) || (TriggerStatus.FAILURE == child.getStatus())));
		if (parentComplete) {
			final boolean parentFail = parent.getChildren().stream().anyMatch(
					child -> (TriggerStatus.FAILURE == child.getStatus()));
			final TriggerStatus parentStatus = parentFail ? TriggerStatus.FAILURE : TriggerStatus.SUCCESS;
			parent.setStatus(parentStatus); // no need to notify anyone?
		}
	}
	public void forceStart(final String triggerName) {
		final Trigger trigger = triggersCache.get(triggerName);
		if (null == trigger) {
			logger.info("NOT found {}", triggerName);
			return;
		}
		if (null == trigger.getJob() || trigger.getJob().isEmpty()) {
			trigger.setStatus(TriggerStatus.RUNNING);
			initiateDependents(trigger);//This will run in various threads or recursively
			return;
		}
		final IJobExecutorService jes = jobExecutors.get(trigger.getQualifier());
		jes.initiateExecute(trigger.getName(), trigger.getJob(), 
				trigger.getArguments(), jr -> jobCallback(trigger, jr));		
	}
	protected JobReturn jobCallback(final Trigger trigger, JobReturn jr) {
		final JobStatus js = jr.getJobStatus();
		final TriggerStatus ts = TriggerStatus.getTriggerStatus(js);
		trigger.setStatus(ts);
		if (JobStatus.SUCCESS == jr.getJobStatus()) {
			initiateDependents(trigger);//This will run in various threads or recursively
			trigger.setStatus(TriggerStatus.SUCCESS);
		}
		return jr;
	}
	public IJobExecutorService setJobExecutor(final String qualifier, final IJobExecutorService service) {
		return jobExecutors.put(qualifier, service);
	}
	public void setTriggersCache(TriggerTreeCache triggersCache) {
		this.triggersCache = triggersCache;
	}
	public void setConditionChecker(TriggerConditionChecker conditionChecker) {
		this.conditionChecker = conditionChecker;
	}
}
