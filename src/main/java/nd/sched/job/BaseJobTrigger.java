package nd.sched.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseJobTrigger implements IJobTrigger{
    protected String parent;
    protected String name;
    protected String additionalArguments;
    protected List<IJobTrigger> interestList = new ArrayList<>();
    protected List<Set<JobTriggerStatus>> interestListStatus = new ArrayList<>();
    protected List<IJobTrigger> children = new ArrayList<>();
    protected List<IJobTrigger> notifyList = new ArrayList<>();
    protected JobTriggerStatus status = JobTriggerStatus.CREATED;
    protected String condition;
    protected String timeCondition;
    protected String description;
    protected String targetJob;
    protected String timezone;
    protected boolean isTime;


    @Override
    public String toString(){
        Object[] cols = {parent,name,timeCondition,condition,targetJob,additionalArguments,description,status};
        return arrayToString(cols);
    }

    public static String arrayToString(Object[] cols) {
        return Arrays
            .stream(cols)
            .map(col -> (null == col) ? "" : col.toString())
            .collect(Collectors.joining(","));
    }

    @Override
    public String getName() {
        return name;
    }
    @Override
    public String getParent() {
        return parent;
    }
    @Override
    public IJobTrigger setCondition(String condition) {
        this.condition = condition;
        return this;
    }
    @Override
    public IJobTrigger setTimeCondition(String condition) {
        timeCondition = condition;
        return this;
    }
    @Override
    public IJobTrigger setArguments(String arguments) {
        additionalArguments = arguments;
        return this;
    }
    @Override
    public List<IJobTrigger> getChildren() {
        return children;
    }
    @Override
    public List<IJobTrigger> getInterestList() {
        return interestList;
    }
    @Override
    public synchronized JobTriggerStatus getStatus() {
        return status;
    }
    @Override
    public IJobTrigger setName(String name) {
        this.name = name;
        return this;
    }
    @Override
    public IJobTrigger setParent(String parent) {
        this.parent = parent;
        return this;
    }
    public IJobTrigger setInterestList(List<IJobTrigger> interestList) {
        this.interestList = interestList;
        return this;
    }
    public IJobTrigger setChildren(List<IJobTrigger> children) {
        this.children = children;
        return this;
    }
    public synchronized IJobTrigger setStatus(JobTriggerStatus status) {
        this.status = status;
        return this;
    }
    @Override
    public String getCondition() {
        return condition;
    }
    @Override
    public String getTimeCondition() {
        return timeCondition;
    }
    @Override
    public String getArguments() {
        return additionalArguments;
    }
    @Override
    public BaseJobTrigger setDescription(String description) {
        this.description = description;
        return this;
    }
    @Override
    public String getDescription() {
        return description;
    }
    @Override
    public IJobTrigger setTargetJob(String targetJob) {
        this.targetJob = targetJob;
        return this;
    }
    @Override
    public String getTargetJob() {
        return targetJob;
    }
    @Override
    public IJobTrigger setTimeZone(String timezone) {
        this.timezone = timezone;
        return this;
    }
    @Override
    public String getTimezone() {
        return timezone;
    }
    public boolean isTime() {
        return isTime;
    }
    public IJobTrigger setTime(boolean isTime) {
        this.isTime = isTime;
        return this;
    }
}