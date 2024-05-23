package org.idempotent.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of DataSource using PostgreSQL as the backend.
 */
public class Postgres implements DataSource {
    private final Connection connection;

    /**
     * Constructor for Postgres.
     *
     * @param url      the PostgreSQL server URL.
     * @param user     the PostgreSQL user.
     * @param password the PostgreSQL user's password.
     */
    public Postgres(String url, String user, String password) {
        try {
            this.connection = DriverManager.getConnection(url, user, password);
            String createTableSQL = "CREATE TABLE IF NOT EXISTS key_value_store (" +
                    "key VARCHAR(255) PRIMARY KEY," +
                    "value TEXT NOT NULL," +
                    "expire_at TIMESTAMP)";
            connection.createStatement().execute(createTableSQL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this::cleanUpExpiredKeys, 20, 20, TimeUnit.SECONDS);
    }

    /**
     * Sets a key with a TTL in the PostgreSQL datastore.
     *
     * @param key       the key to set.
     * @param ttlMillis the TTL in milliseconds.
     */
    @Override
    public void set(String key, long ttlMillis) {
        String upsertSQL = "INSERT INTO key_value_store (key, value, expire_at) VALUES (?, ?, ?) " +
                "ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value, expire_at = EXCLUDED.expire_at";
        try {
            PreparedStatement pstmt = connection.prepareStatement(upsertSQL);
            pstmt.setString(1, key);
            pstmt.setBoolean(2, true);
            if (ttlMillis > 0) {
                pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis() + ttlMillis));
            } else {
                pstmt.setTimestamp(3, null);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the value of a key from the PostgreSQL datastore.
     *
     * @param key the key to retrieve.
     * @return true if the key exists and has not expired, false otherwise.
     */
    @Override
    public boolean get(String key) {
        String selectSQL = "SELECT value FROM key_value_store WHERE key = ? AND (expire_at IS NULL OR expire_at > NOW())";
        try {
            PreparedStatement pstmt = connection.prepareStatement(selectSQL);
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("value") != null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cleans up expired keys from the PostgreSQL datastore.
     */
    public void cleanUpExpiredKeys() {
        String cleanUpSQL = "DELETE FROM key_value_store WHERE expire_at IS NOT NULL AND expire_at <= NOW()";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(cleanUpSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}