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
}
