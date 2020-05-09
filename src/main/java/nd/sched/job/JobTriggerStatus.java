package nd.sched.job;

import java.util.List;

public enum JobTriggerStatus {
    CREATED{
        @Override
        public JobTriggerStatus nextState(IJobTrigger job) {
            if (null == job.getParent() || job.getParent().isEmpty()) {
                job.setStatus(INITIALIZED);
                job.getChildren().forEach(j -> j.setStatus(INITIALIZED));
                return INITIALIZED;
            }
            return this;
        }
    }, 
    INITIALIZED{
        @Override
        public JobTriggerStatus nextState(IJobTrigger job) {
            if (job.isConditionSatisfied()) {
                job.setStatus(WAITING);
                return WAITING;
            }
            return this;
        }
    }, 
    WAITING {
        @Override
        public JobTriggerStatus nextState(IJobTrigger job) {
            final String tgt = job.getTargetJob();
            if (null == tgt || tgt.isEmpty()) {
                job.setStatus(RUNNING);
                job.getChildren().forEach(j -> j.setStatus(INITIALIZED));
                return RUNNING;
            }
            return this;
        }
    }, 
    RUNNING {
        @Override
        public JobTriggerStatus nextState(IJobTrigger job) {
            final String tgt = job.getTargetJob();
            if (null != tgt && ! tgt.isEmpty()) {
                return this;
            }
            final List<IJobTrigger> children = job.getChildren();
            boolean allSuccess = children
                .stream()
                .allMatch(j -> (SUCCESS == j.getStatus()));
            if (allSuccess) {
                job.setStatus(SUCCESS);
                return SUCCESS;
            }
            boolean anyWaitRun = children
                .stream()
                .anyMatch(j -> (RUNNING == j.getStatus() || WAITING == j.getStatus()) || INITIALIZED == j.getStatus());
            if (anyWaitRun) {
                return this;
            }
            boolean anyFail = children
                .stream()
                .anyMatch(j -> (FAILURE == j.getStatus()));
            if (anyFail) {
                job.setStatus(FAILURE);
                return FAILURE;
            }
            return this;
        }
    }, 
    SUCCESS,
    FAILURE;
    public JobTriggerStatus nextState(IJobTrigger job) {
        return this;//This is a final state, only can be force started now
    }
}