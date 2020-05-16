package nd.sched.job;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobTrigger extends BaseJobTrigger {
    private static final Logger logger = LoggerFactory.getLogger(JobTrigger.class);

    @Override
    public boolean isConditionSatisfied() {
        boolean bret = true;
        bret = IntStream
            .range(0, interestList.size())
            .allMatch(idx -> interestListStatus.get(idx).contains(interestList.get(idx).getStatus()));
        bret = bret && isTime && (JobTriggerStatus.INITIALIZED == status);
        return bret;
    }
    @Override
    public void computeCondition(Map<String, IJobTrigger> jobsHash) {
        if (null != parent && ! parent.isEmpty()) {
            final JobTrigger jParent = (JobTrigger)jobsHash.get(getParent());
            addInterestingJob(jParent,JobTriggerStatus.RUNNING);
            jParent.getChildren().add(this);
        }
        final String[] condition = getCondition().split("\t");
        Arrays
            .asList(condition)
            .stream()
            .filter(jD -> !"".equals(jD))
            .forEach(jD -> {
                final JobTrigger jDep = (JobTrigger)jobsHash.get(jD);
                if (null == jDep) {
                    logger.error("Could NOT find job: {}", jD);
                    return;
                }
                addInterestingJob(jDep, JobTriggerStatus.SUCCESS);
            });
        isTime = true;
        if (null != timeCondition && ! timeCondition.isEmpty()) {
            isTime = false;
        }
    }
    private void addInterestingJob(final JobTrigger jDep, JobTriggerStatus js) {
        getInterestList().add(jDep);
        Set<JobTriggerStatus> statusSet = new HashSet<>();
        statusSet.add(js);
        interestListStatus.add(statusSet);
        jDep.notifyList.add(this);
    }
}