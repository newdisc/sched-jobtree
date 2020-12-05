package nd.sched.trigger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

public class TriggerDAO {
	private static final Logger logger = LoggerFactory.getLogger(TriggerDAO.class);
	private static final String TRIGGERS_FILE = "";
    private Properties configuration;
    private TriggerTreeCache triggersCache;
	public TriggerDAO addTrigger(Trigger trigger) {
		final Trigger oldTrigger = triggersCache.put(trigger.getName(), trigger);
		if (null != oldTrigger) {
			logger.error("Overwrote trigger: old: {} vs new: {}", oldTrigger, trigger);
		}
		return this;
	}
    public void registerTriggers() {
    	final String filename = configuration.getProperty(TRIGGERS_FILE, "triggerList.csv");
    	final File fileh = new File(filename);
    	if (!fileh.canRead()) {
    		logger.error("Missing executors list file: {}", filename);
    		return;
    	}
    	//parent,name,qualifier,condition,targetJob,additionalArguments,description
    	try (final CSVReader reader = new CSVReader(new FileReader(filename))){
    		reader.skip(1);//header
    		reader.forEach(line -> {
    			final Trigger trigger = new Trigger();
    			trigger.setParent(line[0]);
    			trigger.setName(line[1]);
    			trigger.setQualifier(line[2]);
    			trigger.setDependencies(line[3]);
    			trigger.setJob(line[4]);
    			trigger.setArguments(line[5]);
    			addTrigger(trigger);
    		});
    	} catch (IOException e) {
            final String msg = "Issue reading Job Triggers File: " + filename;
            logger.error(msg, e);
    	}
    }
    public TriggerDAO setConfiguration(Properties configuration) {
		this.configuration = configuration;
		return this;
	}
	public void setTriggersCache(TriggerTreeCache triggerCache) {
		this.triggersCache = triggerCache;
	}
}
