package nd.sched.job;

import java.util.List;
import java.util.Map;

public interface IJobTrigger {
    public void setStatus(JobTriggerStatus status);
    public void setName(final String name);
    public void setParent(String parent);
    public void setCondition(final String condition);
    public void setTimeCondition(final String condition);
    public void setArguments(final String arguments);
    public void setDescription(final String description);
    public void setTargetJob(final String targetJob);
    public void setTimeZone(final String timezone);

    public JobTriggerStatus getStatus();
    public String getName();
    public String getParent();
    public String getCondition();
    public String getTimeCondition();
    public String getArguments();
    public String getDescription();
    public String getTargetJob();
    public String getTimezone();

    public boolean isConditionSatisfied();
    public void computeCondition(Map<String, IJobTrigger> jobsHash);
    public List<IJobTrigger> getChildren();
    public List<IJobTrigger> getInterestList();
}