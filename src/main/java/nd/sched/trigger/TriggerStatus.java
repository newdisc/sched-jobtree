package nd.sched.trigger;

import nd.sched.job.JobReturn.JobStatus;

public enum TriggerStatus {
	CREATED, RUNNING, SUCCESS, FAILURE, HOLD;
	public static TriggerStatus getTriggerStatus(final JobStatus js) {
		final TriggerStatus ts;
		switch (js) {
		case SUCCESS:
			ts = TriggerStatus.SUCCESS;
			break;
		case FAILURE:
			ts = TriggerStatus.FAILURE;
			break;
		case RUNNING:
			ts = TriggerStatus.RUNNING;
			break;
		default:
			ts = TriggerStatus.RUNNING;
			break;
		}
		return ts;
	}
}
