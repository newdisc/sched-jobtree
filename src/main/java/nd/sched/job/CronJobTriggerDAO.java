package nd.sched.job;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nd.sched.job.factory.JobRegistryPopulator;

public class CronJobTriggerDAO {
    private static final Logger logger = LoggerFactory.getLogger(CronJobTriggerDAO.class);
    public static final String TRIGGER_FILE = "triggers.csv";
    public List<? extends IJobTrigger> registerJobTriggers(){
        logger.debug("Loading Triggers from triggers.csv");
        List<? extends IJobTrigger> triggers = JobRegistryPopulator.createBeans("./" + TRIGGER_FILE, JobTrigger.class);
        logger.info("Triggers found: ");
        triggers
            .stream()
            .map(IJobTrigger::toString)
            .forEach(logger::info);
        return triggers;
    }

}