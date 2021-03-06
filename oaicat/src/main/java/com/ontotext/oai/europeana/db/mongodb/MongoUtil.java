package com.ontotext.oai.europeana.db.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

import java.util.Date;

import static com.ontotext.oai.europeana.db.mongodb.RegistryFields.KEY_COLLECTION;
import static com.ontotext.oai.europeana.db.mongodb.RegistryFields.KEY_DATE;
import static com.ontotext.oai.europeana.db.mongodb.RegistryFields.KEY_RECORD_ID;

/**
 * Created by Simo on 13-12-12.
 */

/**
 * Simo: All methods can be static if field names are static but prefer to let it as it is.
 * Queries can be cached.
 */
public class MongoUtil {


    public DBObject queryDateRange(String setId, Date from, Date until) {
        DBObject query = new BasicDBObject();
        // from date has 000 milliseconds. Adding 1000 and '<' comparison selects the entire second from .000 to .999
        query.put(KEY_DATE, BasicDBObjectBuilder.start("$gte", from).add("$lt", new Date(until.getTime() + 1000L)).get());
        if (setId != null) {
            query.put(KEY_COLLECTION, setId);
        }
        return query;
    }

    public DBObject queryRecord(String recordId) {
        DBObject query = new BasicDBObject();
        query.put(KEY_RECORD_ID,  recordId);
        return query;
    }

    public DBObject queryIndexDate() {
        DBObject query = new BasicDBObject();
        query.put(KEY_DATE, 1);
        return query;
    }

    public DBObject queryIndexSetDate() {
        DBObject query = new BasicDBObject();
        query.put(KEY_COLLECTION, 1);
        query.put(KEY_DATE, 1);
        return query;
    }
}
