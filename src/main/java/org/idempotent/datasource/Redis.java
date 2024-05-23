package org.idempotent.datasource;

import redis.clients.jedis.Jedis;
/**
 * Implementation of DataSource using Redis as the backend.
 */
public class Redis implements DataSource {
    private final Jedis jedis;

    /**
     * Constructor for Redis.
     *
     * @param host the Redis server host.
     * @param port the Redis server port.
     */
    public Redis(String host, int port) {
        this.jedis = new Jedis(host, port);
    }

    /**
     * Sets a key with a TTL in the Redis datastore.
     *
     * @param key the key to set.
     * @param ttl the TTL in milliseconds.
     */
    @Override
    public void set(String key, long ttl) {
        jedis.set(key, "true");
        if (ttl > 0) {
            jedis.pexpire(key, ttl);
        }
    }

    /**
     * Retrieves the value of a key from the Redis datastore.
     *
     * @param key the key to retrieve.
     * @return true if the key exists, false otherwise.
     */
    @Override
    public boolean get(String key) {
        return Boolean.TRUE.equals(jedis.exists(key));
    }
}
