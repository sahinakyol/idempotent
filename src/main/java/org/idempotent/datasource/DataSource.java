package org.idempotent.datasource;

/**
 * Interface for data source implementations.
 */
public interface DataSource {
    /**
     * Sets a key with a TTL in the data source.
     *
     * @param key the key to set.
     * @param ttl the TTL in milliseconds.
     */
    void set(String key, long ttl);

    /**
     * Retrieves the value of a key from the data source.
     *
     * @param key the key to retrieve.
     * @return true if the key exists, false otherwise.
     */
    boolean get(String key);
}