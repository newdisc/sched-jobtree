package nd.sched.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseJobTrigger implements IJobTrigger{
    //private static final Logger logger = LoggerFactory.getLogger(BaseJobTrigger.class);
    protected String parent;
    protected String name;
    protected String additionalArguments;
    //private IJobTrigger parentLink;
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
    public void setCondition(String condition) {
        this.condition = condition;
    }
    @Override
    public void setTimeCondition(String condition) {
        timeCondition = condition;
    }
    @Override
    public void setArguments(String arguments) {
        additionalArguments = arguments;
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
    public JobTriggerStatus getStatus() {
        return status;
    }
    @Override
    public void setName(String name) {
        this.name = name;
    }
    @Override
    public void setParent(String parent) {
        this.parent = parent;
    }
    public void setInterestList(List<IJobTrigger> interestList) {
        this.interestList = interestList;
    }
    public void setChildren(List<IJobTrigger> children) {
        this.children = children;
    }
    public synchronized void setStatus(JobTriggerStatus status) {
        this.status = status;
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
    public void setDescription(String description) {
        this.description = description;
    }
    @Override
    public String getDescription() {
        return description;
    }
    @Override
    public void setTargetJob(String targetJob) {
        this.targetJob = targetJob;
    }
    @Override
    public String getTargetJob() {
        return targetJob;
    }
    @Override
    public void setTimeZone(String timezone) {
        this.timezone = timezone;
    }
    @Override
    public String getTimezone() {
        return timezone;
    }
    public boolean isTime() {
        return isTime;
    }
    public void setTime(boolean isTime) {
        this.isTime = isTime;
    }
}