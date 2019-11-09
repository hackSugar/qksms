package org.hacksugar.cryptor;

import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import java.util.Set;

public class MongoConnect {
    public MongoConnect() {
        try {
            MongoClientURI uri  = new MongoClientURI("mongodb://admin:testpass@18.217.149.1:27017/weasel");
            MongoClient client = new MongoClient(uri);

            DB db = client.getDB("weasel");
            Set<String> collectionNames = db.getCollectionNames();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
