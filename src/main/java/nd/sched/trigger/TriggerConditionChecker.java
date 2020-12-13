package nd.sched.trigger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriggerConditionChecker {
	private static Logger logger = LoggerFactory.getLogger(TriggerConditionChecker.class);
	private TriggerTreeCache triggers;
	public void setTriggersCache(TriggerTreeCache triggers) {
		this.triggers = triggers;
	}
	
	public List<String> getDependents(String dependentLine) {
		return Arrays.asList(dependentLine.split(",", -1));
	}
	public boolean isParentRunning(final Trigger tocheck) {
		boolean ret = true;
		String stat = "UNKNOWN";
		final Trigger parent = triggers.get(tocheck.getParent());
		if (null != parent) {
			final TriggerStatus pstat = parent.getStatus();
			stat = pstat.name();
			if (TriggerStatus.RUNNING != pstat) {
				ret = false;
			}
		}
		logger.info("Parent check: {} Parent: {} Status: {}", tocheck.getName(), tocheck.getParent(), stat);
		return ret;
	}
	public boolean isTriggerConditionsOK(final Trigger tocheck) {
		final String current = tocheck.getName();
		logger.info("Checking ok condition for : {}", current);
		if (!isParentRunning(tocheck)) {
			return false;
		}
		final String depstr = tocheck.getDependencies();
		final List<Trigger> dependents = getDependents(depstr)
				.stream()
				.map(triggers::get)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		if (null != depstr && !"".equals(depstr)) {
			if (0 == dependents.size()) {
				logger.error("Did NOT find any dependents from: {}", depstr);
			}
		}
		boolean allNotSuccess = dependents.stream()
			.anyMatch(trig -> {
				final TriggerStatus ts = trig.getStatus();
				logger.info("Dependencies check : {} check: {} Status: {}", current, trig.getName(), ts);
				return (TriggerStatus.SUCCESS != trig.getStatus());
			})
			;
		return !allNotSuccess;
	}
}
