package nd.sched.trigger;

import java.util.HashMap;

public class TriggerTreeCache extends HashMap<String, Trigger> {
	private static final long serialVersionUID = 1L;
	public boolean addInterested(Trigger trigger, final String dependent) {
		final Trigger dep = get(dependent);
		if (null == dep) {
			return false;
		}
		dep.addInterested(trigger);
		return true;
	}
	public void addParent(Trigger trigger) {
		final Trigger parent = get(trigger.getParent());
		if (null == parent) {
			return;
		}
		parent.addChild(trigger);
	}
	public TriggerTreeCache updateTriggersInterest(final TriggerConditionChecker conditionChecker) {
		values().stream().forEach(trigger -> {
			addInterested(trigger, trigger.getParent());
			addParent(trigger);
			conditionChecker.getDependents(trigger.getDependencies())
				.forEach(dep -> addInterested(trigger, dep));
		});
		return this;
	}
}
