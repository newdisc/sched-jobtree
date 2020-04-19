package nd.sched.service;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nd.sched.job.JobTrigger;
import nd.sched.job.service.QuartzCronService;

public class QuartzCronServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(QuartzCronServiceTest.class);

    @Test
    public void addJobTest() throws SchedulerException {
        try (final QuartzCronService qcs = new QuartzCronService();) {
           final JobTrigger jt = new JobTrigger();
        /*
         * parent,name,timeCondition,condition,targetJob,additionalArguments,description
         * ,timezone ,root,0 0/5 0 ? * * *,,,arguments Unused,Daily Run box at5 0 ? *
         * 1-5,Europe/London
         */
            jt.setName("root");
            jt.setTimeCondition("0/5	*	*	?	*	*	*");
            jt.setDescription("Daily RUn box");
            jt.setTimeZone("Europe/London");
            qcs.addJob(jt);
            qcs.start();
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                logger.error("InterruptedException", e);
            }
        } catch (IOException e) {
            logger.error("InterruptedException", e);
        }
    }
}