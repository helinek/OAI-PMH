package com.ontotext.process.list;

import com.ontotext.process.ListProcessor;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import se.kb.oai.pmh.RecordsList;

import java.util.Properties;

/**
 * Created by Simo on 14-2-11.
 */
public class TimeMeasureProcessor implements ListProcessor {
    private static Log log = LogFactory.getLog(TimeMeasureProcessor.class);
    long lastTime = System.currentTimeMillis();
    long totalTime = 0L;
    private long listCount = 0L;

    public TimeMeasureProcessor(Properties properties) {
    }

    public void processListBegin(RecordsList recordsList) {
        long time = System.currentTimeMillis();
        long diff = time - lastTime;
        totalTime += diff;
        ++listCount;
        lastTime = time;
        log.info(diff);
    }

    public void processListEnd(RecordsList recordsList) {

    }

    public void processListFinish() {
        log.info("Total pages: " + listCount);
        log.info("Total time: " + DurationFormatUtils.formatDuration(totalTime, "HH:mm:ss.SSS"));
        log.info("Average time: " + DurationFormatUtils.formatDuration(totalTime/listCount, "mm:ss.SSS"));
    }

    public void processListError(Exception e) {
        processListFinish();
    }
}
