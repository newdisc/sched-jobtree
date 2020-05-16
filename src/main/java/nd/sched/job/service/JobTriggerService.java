package nd.sched.job.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nd.sched.job.CronJobTriggerDAO;
import nd.sched.job.IJobTrigger;
import nd.sched.job.JobTriggerStatus;
import nd.sched.job.IJobExecutor.JobReturn;
import nd.sched.job.IJobExecutor.JobStatus;

public class JobTriggerService {
    private static final Logger logger = LoggerFactory.getLogger(JobTriggerService.class);
    public static final String ENDLOOP_JOB = "DUMMY_SHUTDOWN_TRIGGER";

    private CronJobTriggerDAO dao = new CronJobTriggerDAO();
    private List<? extends IJobTrigger> jobList;
    private AsyncExecutorFacade executorFacade;
    private BlockingQueue<String> jobTriggerQueue;

    public void loadTriggers(QuartzCronService quartzCronService) {
        List<? extends IJobTrigger> jobs = dao.registerJobTriggers();
        jobList = jobs;
        Map<String, IJobTrigger> jobsHash = jobs.stream().collect(Collectors.toMap(IJobTrigger::getName, j -> j));
        if (logger.isDebugEnabled()) {
            logger.debug("Created jobs: {}", jobsHash);
        }
        // Handle parent and interest list
        jobList.stream().forEach(job -> {
            job.computeCondition(jobsHash);
            if (null != job.getTimeCondition() && !job.getTimeCondition().isEmpty()) {
                quartzCronService.addJob(job);
            }
        });
    }

    public JobTriggerStatus runJob(final String triggerName) {
        logger.info("Running JobTrigger: {}", triggerName);
        final List<? extends IJobTrigger> triggers = getJobList();
        final Optional<? extends IJobTrigger> jobtrigger = triggers.stream()
                .filter(jt -> (triggerName.equals(jt.getName()))).findAny();
        if (jobtrigger.isEmpty()) {
            return JobTriggerStatus.FAILURE;
        }
        jobtrigger.get().setStatus(JobTriggerStatus.WAITING);
        return JobTriggerStatus.WAITING;
    }

    public void initiateRun() throws InterruptedException, ExecutionException {
        logger.info("initiateRun loop: ==========");
        List<CompletableFuture<JobReturn>> runningJobs = new ArrayList<>();
        boolean bLoop = true;
        while (bLoop) {
            final String triggerJob = jobTriggerQueue.poll(1, TimeUnit.MINUTES);
            if (null != triggerJob && ENDLOOP_JOB.equals(triggerJob)) {
                bLoop = false;
            }
            if (null != triggerJob) {
                logger.info("Handling Job complete of: {}", triggerJob);
            } else {
                logger.info("Handling timeout on wait");
            }
            int nPendingJobs = jobList.stream()
                .filter(j -> !j.getStatus().isFinal()).collect(Collectors.toList())
                .size();
            if (0 == nPendingJobs) {
                logger.info("Invalid/Handled wakeup: {}", triggerJob);
                continue;
            }

            runningJobs = runningJobs.stream()
                .filter(j -> {
                    boolean isDone = j.isDone();
                    return !isDone;
                }) // jobs that are running are not done
                .collect(Collectors.toList());
            final List<IJobTrigger> waitJobs = determineJobsToRun();

            final List<CompletableFuture<JobReturn>> runJobs = waitJobs.stream()
                .map(this::executeJob)
                //.map(j -> executeJob((IJobTrigger) j))
                .collect(Collectors.toList());
            runningJobs.addAll(runJobs);

            if (!runningJobs.isEmpty()) {
                JobReturn jr = (JobReturn) CompletableFuture
                    .anyOf(runningJobs.toArray(new CompletableFuture[runningJobs.size()])).get();
                logger.info("One job returned: {}", jr.getJobStatus());
            }
            logger.info("------------Done jobs trigger: {}----------", triggerJob);
        }
    }

    private List<IJobTrigger> determineJobsToRun() {
        final List<IJobTrigger> waitJobs = jobList.stream().map(j -> {
            final JobTriggerStatus cStatus = j.getStatus();
            final JobTriggerStatus nStatus = cStatus.nextState(j);
            if (cStatus != nStatus) {
                return j;
            }
            if (JobTriggerStatus.WAITING == j.getStatus()) {
                return j;
            }
            return null;
        })
        .filter(j -> (null != j && JobTriggerStatus.WAITING == j.getStatus()))
        .filter(j -> (null == j.getChildren() || j.getChildren().isEmpty())) // skip parentJobs
        .collect(Collectors.toList());
        logger.info("------------Run following jobs: -----------");
        waitJobs.forEach(j -> logger.info(j.toString()));
        return waitJobs;
    }

    public CompletableFuture<JobReturn> executeJob(IJobTrigger j) {
        j.setStatus(JobTriggerStatus.RUNNING);
        final String tgtJob = j.getTargetJob();
        final String args = j.getArguments();
        final CompletableFuture<JobReturn> fjr = executorFacade
            .execute(j.getName(), tgtJob, args)
            .thenApply(jr -> onJobComplete(j, jr));
        return fjr;
    }

    private JobReturn onJobComplete(IJobTrigger j, JobReturn jr) {
        final String jobName = j.getName();
        signalJob(jobName);
        if (JobStatus.SUCCESS == jr.getJobStatus()) {
            j.setStatus(JobTriggerStatus.SUCCESS);
        } else {
            j.setStatus(JobTriggerStatus.FAILURE);
        }
        return jr;
    }

    public void signalJob(final String jobName) {
        try {
            jobTriggerQueue.put(jobName);
        } catch (InterruptedException e) {
            final String msg = "Issue signalling the JobTriggerQueue!";
            logger.error(msg, e);
        }
    }
    public AsyncExecutorFacade getExecutorFacade() {
        return executorFacade;
    }
    public void setExecutorFacade(AsyncExecutorFacade executorFacade) {
        this.executorFacade = executorFacade;
    }
    public List<? extends IJobTrigger> getJobList() {
        return jobList;
    }
    public void setJobTriggerQueue(BlockingQueue<String> jobTriggerQueue) {
        this.jobTriggerQueue = jobTriggerQueue;
    }
}
