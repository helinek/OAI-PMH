package com.ontotext;

import com.ontotext.process.list.ListProcessorHub;
import com.ontotext.process.record.EmptyRecordProcessor;
import com.ontotext.query.QueryListRecords;
import com.ontotext.walk.ListRecordsWalker;
import com.ontotext.walk.Navigator;
import com.ontotext.walk.PageCountNavigator;
import com.ontotext.walk.StandardNavigator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.RecordsList;

import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.OperationsException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Simo
 * Date: 13-10-30
 * Time: 13:51
 */
public class Main implements Runnable {
    private static final Log log = LogFactory.getLog(Main.class);

    QueryListRecords query;
    OaiPmhServer server;
    ListProcessorHub listProcessor;
    int numPages;

    Main(Properties properties) {
        query = QueryListRecords.load(properties);
        String serverUrl = properties.getProperty("server", "http://localhost:8080/oaicat/OAIHandler");
        server = new OaiPmhServer(serverUrl);
        listProcessor = ListProcessorHub.load(properties);
        numPages = Integer.parseInt(properties.getProperty("numPages", "0"));
    }

    private static Properties loadProperties(String[] args) {
        Properties properties = new Properties();
        final String fileName = (args.length == 0) ? "client.properties" : args[0];
        try {
            FileInputStream input = new FileInputStream(fileName);
            try {
                properties.load(input);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                input.close();
            }
        } catch (IOException e) {
            log.error(e);
        }
        return properties;
    }

    private void registerMBean(Navigator navigator) throws OperationsException, MBeanException {
        OaiClientControl control = new OaiClientControl(navigator);
        ObjectName objectName = new ObjectName("com.ontotext:type=OaiClientControl");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.registerMBean(control,  objectName);
    }

    public void run() {
        Navigator<RecordsList> navigator = new StandardNavigator();
        try {
            List<String> sets = FileUtils.readLines(new File("sets.txt"), "UTF-8");
            if (!sets.isEmpty()) {
                for (String set : sets) {
                    try {
                        log.info("Start set: " + set);
                        QueryListRecords setQuery = new QueryListRecords(query.from, query.until, set);
                        ListRecordsWalker walker = new ListRecordsWalker(
                                server, new EmptyRecordProcessor(), listProcessor, setQuery, navigator);
                        walker.run();
                    } catch (Exception e) {
                        log.error("Set: " + set, e);
                        throw new RuntimeException(e);
                    }
                    finally {
                        listProcessor.processListFinish();
                    }
                    log.info("End set: " + set);
                }
            } else {
                run1Query();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void run1Query() {
        Navigator<RecordsList> navigator = (numPages == 0)
                ? new StandardNavigator()
                : new PageCountNavigator(numPages);

        try {
            registerMBean(navigator);
        } catch (OperationsException e) {
            e.printStackTrace();
        } catch (MBeanException e) {
            e.printStackTrace();
        }

        ListRecordsWalker walker = new ListRecordsWalker(
                server, new EmptyRecordProcessor(), listProcessor, query, navigator);
        try {
            walker.run();
        } finally {
            listProcessor.processListFinish();
        }
    }

    public static void main(String[] args) {
        Properties properties = loadProperties(args);
        Main main = new Main(properties);
        log.info("Begin: " + new Date());
        main.run();
        log.info("End: " + new Date());
    }
}
