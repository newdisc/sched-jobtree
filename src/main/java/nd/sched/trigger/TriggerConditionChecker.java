package nd.sched.trigger;

import java.util.Arrays;
import java.util.List;

public class TriggerConditionChecker {
	private TriggerTreeCache triggers;
	public void setTriggers(TriggerTreeCache triggers) {
		this.triggers = triggers;
	}
	
	public List<String> getDependents(String dependentLine) {
		return Arrays.asList(dependentLine.split(",", -1));
	}
	public boolean isParentRunning(final Trigger tocheck) {
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
			.anyMatch(trig -> (trig == null || TriggerStatus.SUCCESS != trig.getStatus()))
			;
		return !allNotSuccess;
	}
}
