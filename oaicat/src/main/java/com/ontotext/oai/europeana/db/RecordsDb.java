package com.ontotext.oai.europeana.db;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.exceptions.MongoDBException;
import eu.europeana.corelib.solr.server.EdmMongoServer;
import eu.europeana.corelib.solr.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.solr.utils.EdmUtils;

import java.net.UnknownHostException;
import java.util.Properties;

/**
 * Created by Simo on 14-2-13.
 */
public class RecordsDb implements RecordsProvider {
    private EdmMongoServer edmServer;

    public RecordsDb(Properties properties) {
        String host = properties.getProperty("RecordsDb.host", "localhost");
        int port = Integer.parseInt(properties.getProperty("RecordsDb.port", "27017"));
        String databaseName = properties.getProperty("RecordsDb.dbname", "europeana");
        String username = properties.getProperty("RecordsDb.username", "");
        String password = properties.getProperty("RecordsDb.password", "");

        try {
            Mongo mongo = new MongoClient(host, port);
            edmServer = new EdmMongoServerImpl(mongo, databaseName, username, password);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoDBException e) {
            e.printStackTrace();
        }
    }
    // id: /11601/database_detail_php_ID_187548 ->
    // http://europeana.eu/api/v2/record/11601/database_detail_php_ID_187548.rdf?wskey=api2demo
    public String getRecord(String id) {
        String rdf = null;
        try {
            FullBeanImpl fullBean = (FullBeanImpl) edmServer.getFullBean(id);
            if (fullBean != null) {
                rdf = EdmUtils.toEDM(fullBean, false);
            }

        } catch (MongoDBException e) {
            e.printStackTrace();
        }

        return rdf;
    }

    public void close() {
        edmServer.close();
    }

    public static void main(String[] args) {
//        String recordId = "http://data.europeana.eu/item/11601/HERBARW_NHMV_AUSTRIA_187548";
        String recordId = "/item/91647/2A35E4A2EA4495D0A5F3DEC6B9D92E13E17C7D81";
//        String recordId = "/11601/database_detail_php_ID_187548";
        Properties properties = new Properties();
        RecordsDb db = new RecordsDb(properties);
        try {
            String rdf = db.getRecord(recordId);
            System.out.println(rdf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            db.close();
        }
    }
}