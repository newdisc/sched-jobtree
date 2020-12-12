package nd.sched.trigger;

import java.util.Arrays;
import java.util.List;

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
		logger.info("Checking: {} Parent: {}", tocheck.getName(), tocheck.getParent());
		final Trigger parent = triggers.get(tocheck.getParent());
		if (null != parent) {
			final TriggerStatus pstat = parent.getStatus();
			if (TriggerStatus.RUNNING != pstat) {
				return false;
			}
		}
		return true;
	}
	public boolean isTriggerConditionsOK(final Trigger tocheck) {
		if (!isParentRunning(tocheck)) {
			return false;
		}
		boolean allNotSuccess = getDependents(tocheck.getDependencies())
			.stream()
			.map(triggers::get)
			.filter(trig -> (null != trig))
			.anyMatch(trig -> {
				final TriggerStatus ts = trig.getStatus();
				logger.info("Checking: {} Status: {}", trig.getName(), ts);
				return (TriggerStatus.SUCCESS != trig.getStatus());
			})
			;
		return !allNotSuccess;
	}
}
