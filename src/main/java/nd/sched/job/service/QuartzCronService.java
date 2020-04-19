package nd.sched.job.service;

import java.io.Closeable;
import java.io.IOException;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nd.sched.job.IJobTrigger;
import nd.sched.job.quartz.QuartzJob;

public class QuartzCronService implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(QuartzCronService.class);
    private final SchedulerFactory schedulerFactory;
    private final Scheduler scheduler;

    public QuartzCronService() throws SchedulerException {
        schedulerFactory = new StdSchedulerFactory();
        scheduler = schedulerFactory.getScheduler();
        start();
    }
    public void start() throws SchedulerException{
        scheduler.start();
    }

    public void addJob(IJobTrigger trigger) {
        final String name = trigger.getName() + "_quartz";
        final JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("jobTrigger", trigger);
        JobDetail jd = JobBuilder
            .newJob(QuartzJob.class)
            .usingJobData(jobDataMap)
            .withIdentity(name, "JobDetails")
            .withDescription(trigger.getDescription())
            .build();
        //final ScheduleBuilder<CronTrigger> sb = CronScheduleBuilder.cronSchedule(trigger.getTimeCondition());
        final Trigger trg = TriggerBuilder
            .newTrigger()
            .withIdentity(name, "Triggers")
            .withSchedule(CronScheduleBuilder.cronSchedule(trigger.getTimeCondition()))
            .build();

        try {
            scheduler.scheduleJob(jd, trg);
        } catch (SchedulerException e) {
            final String msg = "Unable to add time schedule for job: " + name;
            logger.error(msg, e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            scheduler.shutdown(true);
        } catch (SchedulerException e) {
            final String msg = "Unable to Shutdown scheduler";
            logger.error(msg, e);
        }
    }
}