package nd.sched.job.service;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobTriggerWorker implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(JobTriggerWorker.class);
    private final ExecutorService javaExecutorService = Executors.newSingleThreadExecutor();
    private JobTriggerService jobTriggerService;

    public void serviceThread() {
        Thread current = Thread.currentThread();
        current.setName("JobTriggerWorker-" + current.getId());
        logger.info("Running thread");
        try {
            jobTriggerService.initiateRun();
        } catch (InterruptedException | ExecutionException e) {
            final String msg = "Issue running jobs!";
            logger.error(msg, e);
        }
        if (javaExecutorService.isShutdown()) {
            logger.info("Exiting JobTriggerWorker : {}", current.getName());
        }
    }

    @Override
    public void close() throws IOException {
        logger.info("Shutting down JobTriggerWorker thread");
        jobTriggerService.signalJob(JobTriggerService.ENDLOOP_JOB);
        javaExecutorService.shutdown();
    }
    public void setJobTriggerService(JobTriggerService jobTriggerService) {
        this.jobTriggerService = jobTriggerService;
        javaExecutorService.submit(this::serviceThread);
    }
}