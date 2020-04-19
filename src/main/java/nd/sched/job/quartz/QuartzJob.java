package nd.sched.job.quartz;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nd.sched.job.JobTrigger;
import nd.sched.job.JobTriggerStatus;

public class QuartzJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(QuartzJob.class);
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap(); 
        JobTrigger trigger = (JobTrigger)jobDataMap.get("jobTrigger");
        logger.info("Checking to Set timer job to Waiting: {}", trigger.getName());
        trigger.setTime(true);
        final JobTriggerStatus stat = trigger.getStatus();
        if (JobTriggerStatus.RUNNING != stat) {
            logger.info("Checking to Set timer job to Waiting: {} from: ", trigger.getName(), stat);
            trigger.setStatus(JobTriggerStatus.WAITING);
        }
    }
}