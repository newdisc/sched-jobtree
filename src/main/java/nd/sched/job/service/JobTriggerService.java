package nd.sched.job.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
        for (int i = 0; i < 8; i++) {
            List<? extends IJobTrigger> waitJobs = jobList.stream().map(j -> {
                final JobTriggerStatus cStatus = j.getStatus();
                final JobTriggerStatus nStatus = cStatus.nextState(j);
                if (cStatus != nStatus) {
                    return j;
                }
                return null;
            }).filter(j -> (null != j && JobTriggerStatus.WAITING == j.getStatus()))
                    .filter(j -> (null == j.getChildren() || 0 == j.getChildren().size())).collect(Collectors.toList());

            logger.info("------------Run jobs: {}-----------", i);
            waitJobs.forEach(j -> {
                executeJob(j);
            });
            waitJobs.forEach(j -> logger.info(j.toString()));
            logger.info("------------Done jobs: {}----------", i);
        }
    }
    public void executeJob(IJobTrigger j){
        j.setStatus(JobTriggerStatus.RUNNING);
        final String tgtJob = j.getTargetJob();
        final String args = j.getArguments();
        final Future<JobReturn> fjr = executorFacade.execute(tgtJob, args);
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
    }
    public AsyncExecutorFacade getExecutorFacade() {
        return executorFacade;
    }
    public void setExecutorFacade(AsyncExecutorFacade executorFacade) {
        this.executorFacade = executorFacade;
    }
}