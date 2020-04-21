package nd.sched.job.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nd.sched.job.CronJobTriggerDAO;
import nd.sched.job.IJobTrigger;
import nd.sched.job.JobTriggerStatus;
import nd.sched.job.IJobExecutor.JobReturn;

public class JobTriggerService {
    private static final Logger logger = LoggerFactory.getLogger(JobTriggerService.class);
    private CronJobTriggerDAO dao = new CronJobTriggerDAO();
    private Map<String, IJobTrigger> jobsHash;
    private List<? extends IJobTrigger> jobList;
    private AsyncExecutorFacade executorFacade;

    public void loadTriggers() {
        List<? extends IJobTrigger> jobs = dao.registerJobTriggers();
        jobList = jobs;
        jobsHash = jobs.stream().collect(Collectors.toMap(j -> j.getName(), j -> j));
        logger.debug("Created jobs: {}", jobsHash.toString());
        // Handle parent and interest list
        jobList.stream().forEach(job -> {
            job.computeCondition(jobsHash);
        });
    }

    public void initiateRun() {
        logger.info("NOW RUNNING: ==========");
        //final Executor exsvc = executorFacade.getExecutor();
        List<CompletableFuture<JobReturn>> runningJobs = new ArrayList<>();
        int i = 0;
        while (true) {
            int nPendingJobs = jobList
                .stream()
                .filter(j -> (JobTriggerStatus.SUCCESS != j.getStatus() && JobTriggerStatus.FAILURE != j.getStatus()))
                .collect(Collectors.toList())
                .size();
            if (0 == nPendingJobs) {
                return;
            }
            runningJobs = runningJobs
                .stream()
                .filter(j -> !j.isDone()) // jobs that are running are not done
                .collect(Collectors.toList());
            final List<IJobTrigger> waitJobs = determineJobsToRun();
            if (0 == waitJobs.size()) {
                safeSleep(10000);
                i++;
                continue;
            }

            final List<CompletableFuture<JobReturn>> runJobs = waitJobs
                    .stream()
                    .map(j -> executeJob((IJobTrigger) j))
                    .collect(Collectors.toList());
            runningJobs.addAll(runJobs);
            try {
                JobReturn jr = (JobReturn) CompletableFuture
                        .anyOf(runningJobs.toArray(new CompletableFuture[waitJobs.size()])).get();
                logger.info("One job returned: {}", jr.jobStatus);
            } catch (InterruptedException | ExecutionException e) {
                final String msg = "Issue waiting for jobs!";
                logger.error(msg, e);
            }
            
            logger.info("------------Done jobs: {}----------", i);
            safeSleep(10000);
            i++;
        }
    }

    private void safeSleep(int nMSec) {
        try {
            Thread.sleep(nMSec);
        } catch (InterruptedException e) {
            final String msg = "Interrupted!";
            logger.error(msg, e);
        }
    }

    private List<IJobTrigger> determineJobsToRun() {
        final List<IJobTrigger> waitJobs = jobList
            .stream()
            .map(j -> {
                final JobTriggerStatus cStatus = j.getStatus();
                final JobTriggerStatus nStatus = cStatus.nextState(j);
                if (cStatus != nStatus) {
                    return j;
                }
                return null;
            })
            .filter(j -> (null != j && JobTriggerStatus.WAITING == j.getStatus()))
            .filter(j -> (null == j.getChildren() || 0 == j.getChildren().size())).collect(Collectors.toList());
        logger.info("------------Run following jobs: -----------");
        waitJobs.forEach(j -> logger.info(j.toString()));
        return waitJobs;
    }
    public CompletableFuture<JobReturn> executeJob(IJobTrigger j){
        j.setStatus(JobTriggerStatus.RUNNING);
        final String tgtJob = j.getTargetJob();
        final String args = j.getArguments();
        final CompletableFuture<JobReturn> fjr = executorFacade
            .execute(tgtJob, args)
            .thenApply(jr -> {
                switch (jr.jobStatus) {
                    case SUCCESS :
                        j.setStatus(JobTriggerStatus.SUCCESS);
                    break;
                    default:
                        j.setStatus(JobTriggerStatus.FAILURE);
                    break;
                } 
                return jr;
            });
        return fjr;
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
}
/*
        JobReturn jr;
        try {
            jr = fjr.get();
            logger.info("Job Returned: {}", jr.returnValue);
            if (JobStatus.SUCCESS == jr.jobStatus) {
                j.setStatus(JobTriggerStatus.SUCCESS);
                return;
            }
        } catch (InterruptedException | ExecutionException e) {
            final String msg = "Failed to retrieve job status - " + tgtJob;
            logger.error(msg,e);
        }
        j.setStatus(JobTriggerStatus.FAILURE);
*/
