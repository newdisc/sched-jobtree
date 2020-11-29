package nd.sched.job;


import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

public class CronJobTriggerDAO {
    private static final Logger logger = LoggerFactory.getLogger(CronJobTriggerDAO.class);
    public static final String TRIGGER_FILE = "triggers.csv";
    public List<? extends IJobTrigger> registerJobTriggers(){
        logger.debug("Loading Triggers from triggers.csv");
        final List<? extends IJobTrigger> ret;
    	try (final CSVReader reader = new CSVReader(new FileReader("./" + TRIGGER_FILE))){
    		reader.skip(1);//header
    		ret = reader.readAll().stream().map(line -> {
    			//parent,name,timeCondition,condition,targetJob,additionalArguments,description,timezone
    			return new JobTrigger(line);//registerExecutor(line[0], line[1], Arrays.copyOfRange(line, 2, line.length));
    		}).collect(Collectors.toList());
    	} catch (IOException | CsvException e) {
            final String msg = "Issue reading Job Triggers File: ./" + TRIGGER_FILE;
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
    	}
    	return ret;
    }

}