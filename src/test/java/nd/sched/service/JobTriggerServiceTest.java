package nd.sched.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nd.sched.job.service.TestAsyncEFCloseable;
import nd.sched.job.service.JobTriggerService;
import nd.sched.job.service.QuartzCronService;

public class JobTriggerServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(JobTriggerServiceTest.class);

    public static class TestJobTriggerSvc extends TestAsyncEFCloseable {
        public JobTriggerService jobTriggerService;
        public QuartzCronService quartzCronService;

        public TestJobTriggerSvc() {
            super();
            jobTriggerService = new JobTriggerService();
            jobTriggerService.setExecutorFacade(asyncSvc);
            try {
                quartzCronService = new QuartzCronService();
            } catch (SchedulerException e) {
                final String msg = "Issue starting the cron service";    
                logger.error(msg, e);
            }
        }
        @Override
        public void close() throws IOException {
            super.close();
            quartzCronService.close();
        }
    }
    @Test
    public void loadTriggersTest(){
        try (TestJobTriggerSvc jtst = new TestJobTriggerSvc();) {
            final JobTriggerService jts = jtst.jobTriggerService;
            final QuartzCronService qcs = jtst.quartzCronService;
            logger.info("Loading Triggers");
            jts.loadTriggers(qcs);
            jts.initiateRun();
            assertTrue(true);
        } catch (IOException e) {
            final String msg = "Issue shutting down the async Service / running the task";    
            logger.error(msg, e);
        } finally {
            logger.info("Async Svc should have shutdown");
        }
    }
}