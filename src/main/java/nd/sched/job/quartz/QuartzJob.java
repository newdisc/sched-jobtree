package nd.sched.job.quartz;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nd.sched.job.JobTrigger;
import nd.sched.job.JobTriggerStatus;
import nd.sched.job.service.JobTriggerService;

public class QuartzJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(QuartzJob.class);
    private final Set<JobTriggerStatus> allowedToWait;
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap(); 
        JobTrigger trigger = (JobTrigger)jobDataMap.get("jobTrigger");
        JobTriggerService jobTriggerService = (JobTriggerService)jobDataMap.get("jobTriggerService");
        final JobTriggerStatus stat = trigger.getStatus();
        logger.info("Checking to Set timer job to Waiting: {} from {}", trigger.getName(), stat);
        jobTriggerService.signalJob(trigger.getName());
        trigger.setTime(true);
        if (allowedToWait.contains(stat)) {
            logger.info("Set timer job to Waiting: {} from: {}", trigger.getName(), stat);
            trigger.setStatus(JobTriggerStatus.WAITING);
        }
    }

    public QuartzJob() {
        JobTriggerStatus[] validStates = {JobTriggerStatus.INITIALIZED, 
            JobTriggerStatus.SUCCESS, JobTriggerStatus.CREATED};
        this.allowedToWait = Arrays.stream(validStates).collect(Collectors.toSet());
    }
}