package nd.sched.trigger.vertx;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import nd.sched.job.vertx.HandlerBase;
import nd.sched.trigger.Trigger;
import nd.sched.trigger.TriggerDAO;
import nd.sched.trigger.TriggerService;
import nd.sched.trigger.TriggerTreeCache;

public class TriggerControllerVertx extends HandlerBase {
	private static final Logger logger = LoggerFactory.getLogger(TriggerControllerVertx.class);
	private static final String CONTENT_TYPE = "content-type";
	private static final String JSON_UTF8 = "application/json; charset=utf-8";
	private TriggerService triggerService;
	private TriggerTreeCache triggerCache;
	private TriggerDAO triggerDAO;
	private Router router;

	//
	public TriggerControllerVertx setRouter(Router router) {
		this.router = router;
		router.get("/list").handler(this::list);
		router.get("/details").handler(this::details);
		router.get("/execute").handler(this::execute);
		router.get("/load").handler(this::load);
		return this;
	}
	
	public void list(final RoutingContext rc){
		logger.info("Trigger List: ");
		final List<Trigger> allTrigs = triggerCache.entrySet().stream()
				.map(Map.Entry<String,Trigger>::getValue).collect(Collectors.toList());
		rc.response()
	      .putHeader(CONTENT_TYPE, JSON_UTF8)
	      .end(Json.encodePrettily(allTrigs));
	}
	
	public void details(final RoutingContext rc){
		final String triggerName = rc.request().getParam("triggerName");
		logger.info("Trigger Details: {}", triggerName);
		final Trigger curTrig = triggerCache.get(triggerName);
		rc.response()
	      .putHeader(CONTENT_TYPE, JSON_UTF8)
	      .end(Json.encodePrettily(curTrig));
	}
	
	public void execute(final RoutingContext rc) {
		final String triggerName = rc.request().getParam("triggerName");
		logger.info("Executing: {}", triggerName);
		triggerService.forceStart(triggerName);
		rc.response()
	      .putHeader(CONTENT_TYPE, JSON_UTF8)
	      .end("{\"return\": \"SUCCESS\"}");
	}

	public void load(final RoutingContext rc){
		logger.info("Job Load: ");
		triggerDAO.registerTriggers();
		rc.response()
	      .putHeader(CONTENT_TYPE, JSON_UTF8)
	      .end("{\"return\": \"SUCCESS\"}");
	}

	public void setTriggerService(TriggerService triggerService) {
		this.triggerService = triggerService;
	}

	public void setTriggerCache(TriggerTreeCache triggerCache) {
		this.triggerCache = triggerCache;
	}

	@Override
	public void handle(RoutingContext event) {
		router.handleContext(event);
	}

	public void setTriggerDAO(TriggerDAO triggerDAO) {
		this.triggerDAO = triggerDAO;
	}
}
