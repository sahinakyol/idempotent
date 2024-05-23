package org.idempotent.datasource;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of DataSource using MongoDB as the backend.
 */
public class MongoDB implements DataSource {
    private final MongoCollection<Document> collection;

    /**
     * Constructor for MongoDB.
     *
     * @param connectionString the MongoDB connection string.
     * @param databaseName     the MongoDB database name.
     * @param collectionName   the MongoDB collection name.
     */
    public MongoDB(String connectionString, String databaseName, String collectionName) {
        MongoClient mongoClient = MongoClients.create(connectionString);
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        this.collection = database.getCollection(collectionName);

        IndexOptions indexOptions = new IndexOptions().expireAfter(0L, TimeUnit.SECONDS);
        collection.createIndex(new Document("expireAt", 1), indexOptions);
    }

    /**
     * Sets a key with a TTL in the MongoDB datastore.
     *
     * @param key the key to set.
     * @param ttl the TTL in milliseconds.
     */
    @Override
    public void set(String key, long ttl) {
        Document document = new Document("_id", key)
                .append("value", true)
                .append("createdAt", new Date());
        if (ttl > 0) {
            document.append("expireAt", new Date(System.currentTimeMillis() + ttl));
        } else {
            document.append("expireAt", null);
        }
        collection.insertOne(document);
    }

    /**
     * Retrieves the value of a key from the MongoDB datastore.
     *
     * @param key the key to retrieve.
     * @return true if the key exists and has not expired, false otherwise.
     */
    @Override
    public boolean get(String key) {
        Document document = collection.find(new Document("_id", key)).first();
        if (document == null) return false;

        return document.getString("value") != null;
    }
}