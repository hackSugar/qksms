package org.hacksugar.cryptor;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoConnect {
    private MongoDatabase db;
    public MongoConnect() {
        MongoClientURI uri = new MongoClientURI( "mongodb://admin:testpass@18.217.149.1:27017/weasel");
        MongoClient mongoClient = new MongoClient(uri);
        db = mongoClient.getDatabase(uri.getDatabase());
    }
    public void addNumber(String hash, String key) {
        MongoCollection<BasicDBObject> collection = db.getCollection("phoneKeys", BasicDBObject.class);
        BasicDBObject document = new BasicDBObject();
        document.put("phone", hash);
        document.put("key", key);
        collection.insertOne(document);
    }
}
