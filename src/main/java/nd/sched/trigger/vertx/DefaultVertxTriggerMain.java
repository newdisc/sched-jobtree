package nd.sched.trigger.vertx;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nd.sched.job.service.IJobExecutorService;
import nd.sched.job.vertx.DefaultVertxMain;
import nd.sched.job.vertx.JobExecutorController;
import nd.sched.job.vertx.VertxVentricleHttp;
import nd.sched.trigger.TriggerConditionChecker;
import nd.sched.trigger.TriggerDAO;
import nd.sched.trigger.TriggerService;
import nd.sched.trigger.TriggerTreeCache;

public class DefaultVertxTriggerMain extends DefaultVertxMain {
	static {
		System.setProperty("logback.configurationFile", "./logback.xml");
		System.setProperty("vertx.options.maxEventLoopExecuteTime", "10000000000");
	}
	private static final Logger logger = LoggerFactory.getLogger(DefaultVertxTriggerMain.class);
	public static void main(String[] args) throws Exception{
		// Create the pieces needed and link together - can use spring instead
		logger.info("Creating default Vertx");
		final JobExecutorController jec = initJobController();
		final TriggerControllerVertx tcv = initTriggerController(jec.getExecutorService());
		final VertxVentricleHttp jev = new VertxVentricleHttp();
		jev.addHandler("/api/job/", jec);
		jev.addHandler("/api/trigger/", tcv);

		runVertx(jev);
	}
	public static TriggerControllerVertx initTriggerController(final IJobExecutorService je) {
		final TriggerConditionChecker tcc = new TriggerConditionChecker();
		final TriggerTreeCache ttc = new TriggerTreeCache();
		tcc.setTriggersCache(ttc);
		final TriggerService ts = new TriggerService();
		ts.setJobExecutor("localhost", je);
		ts.setTriggersCache(ttc);
		ts.setConditionChecker(tcc);

		final TriggerControllerVertx tcv = new TriggerControllerVertx();
		tcv.setTriggerCache(ttc);
		tcv.setTriggerService(ts);
		
		final TriggerDAO tdao = new TriggerDAO();
		final Properties props = new Properties();
		tdao.setConfiguration(props);
		tdao.setTriggersCache(ttc);
		tdao.registerTriggers();
		tcv.setTriggerDAO(tdao);
		
		ts.updateTriggersInterest();
		return tcv;
	}
}
