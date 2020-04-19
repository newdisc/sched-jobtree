package nd.sched;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import nd.sched.job.CronJobTriggerDAO;

public class CronJobTriggerDAOTest {
    @Test
    public void registerJobTriggersTest(){
        final CronJobTriggerDAO cjt = new CronJobTriggerDAO();
        cjt.registerJobTriggers();
        assertTrue(true);
    }
}