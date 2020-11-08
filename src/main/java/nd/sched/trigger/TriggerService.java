package nd.sched.trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriggerService {
	private static final Logger logger = LoggerFactory.getLogger(TriggerService.class);
	private TriggerTreeCache triggers;
	private TriggerConditionChecker conditionChecker;
	public TriggerService addTrigger(Trigger trigger) {
		final Trigger oldTrigger = triggers.put(trigger.getName(), trigger);
		if (null != oldTrigger) {
			logger.error("Overwrote trigger: old: {} vs new: {}", oldTrigger, trigger);
		}
		triggers.addInterested(trigger, trigger.getParent());
		conditionChecker.getDependents(trigger.getDependencies())
			.forEach(dep -> triggers.addInterested(trigger, dep));
		return this;
	}
	public boolean isTriggerConditionsOK(final String triggerName) {
		final Trigger tocheck = triggers.get(triggerName);
		if (null == tocheck) {
			logger.error("Could NOT find trigger: {}", triggerName);
			return false;
		}
		return conditionChecker.isTriggerConditionsOK(tocheck);
	}
}
