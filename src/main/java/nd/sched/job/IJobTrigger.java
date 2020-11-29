package nd.sched.job;

import java.util.List;
import java.util.Map;

public interface IJobTrigger {
    public IJobTrigger setStatus(JobTriggerStatus status);
    public IJobTrigger setName(final String name);
    public IJobTrigger setParent(String parent);
    public IJobTrigger setCondition(final String condition);
    public IJobTrigger setTimeCondition(final String condition);
    public IJobTrigger setArguments(final String arguments);
    public IJobTrigger setDescription(final String description);
    public IJobTrigger setTargetJob(final String targetJob);
    public IJobTrigger setTimeZone(final String timezone);

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