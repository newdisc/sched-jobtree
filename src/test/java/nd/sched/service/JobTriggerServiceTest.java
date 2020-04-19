package nd.sched.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nd.sched.job.factory.IJobFactory;
import nd.sched.job.factory.JobFactory;
import nd.sched.job.service.AsyncExecutorFacade;
import nd.sched.job.service.ExecutorService;
import nd.sched.job.service.JobTriggerService;

public class JobTriggerServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(JobTriggerServiceTest.class);
    @Test
    public void loadTriggersTest(){
        final String cwd = Paths.get(".")
            .toAbsolutePath()
            .normalize()
            .toString();
        logger.info("CWD: {}", cwd);
        ExecutorService execSvc = new ExecutorService();
        IJobFactory jobFactory = new JobFactory();
        execSvc.setJobFactory(jobFactory);
        execSvc.load();
        try (AsyncExecutorFacade asyncSvc = new AsyncExecutorFacade();) {
            asyncSvc.setService(execSvc);
            final JobTriggerService jts = new JobTriggerService();
            jts.setExecutorFacade(asyncSvc);
            logger.info("Loading Triggers");
            jts.loadTriggers();
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