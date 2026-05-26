package com.todoapp.util;

import com.todoapp.config.AppConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages the HikariCP JDBC connection pool.
 *
 * Usage:
 *   Connection conn = DatabaseManager.getConnection();
 *   // ... use conn ...
 *   conn.close(); // returns connection to pool – does NOT close physically
 */
public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static HikariDataSource dataSource;

    static {
        initPool();
    }

    // -----------------------------------------------------------------------
    // Initialisation
    // -----------------------------------------------------------------------

    private static void initPool() {
        HikariConfig config = new HikariConfig();

        String jdbcUrl = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
            AppConfig.getDbHost(),
            AppConfig.getDbPort(),
            AppConfig.getDbName()
        );

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(AppConfig.getDbUser());
        config.setPassword(AppConfig.getDbPassword());
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Pool sizing
        config.setMaximumPoolSize(AppConfig.getPoolMaxSize());
        config.setMinimumIdle(AppConfig.getPoolMinIdle());

        // Timeouts (ms)
        config.setConnectionTimeout(30_000);   // 30 s to obtain connection
        config.setIdleTimeout(600_000);        // 10 min idle before eviction
        config.setMaxLifetime(1_800_000);      // 30 min max lifetime

        // Validation
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("TodoApp-Pool");

        try {
            dataSource = new HikariDataSource(config);
            logger.info("HikariCP pool initialised. URL: {}", jdbcUrl);
        } catch (Exception e) {
            logger.error("Failed to initialise database connection pool!", e);
            throw new RuntimeException("Database pool initialisation failed.", e);
        }
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Borrows a {@link Connection} from the pool.
     * Always call {@code conn.close()} in a finally block to return it.
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /** Gracefully shuts down the pool (called from {@code MainApp.stop()}). */
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed.");
        }
    }

    /** Returns true if the pool is alive and a test query succeeds. */
    public static boolean isHealthy() {
        try (Connection conn = getConnection()) {
            return conn.isValid(2);
        } catch (SQLException e) {
            logger.warn("Database health check failed.", e);
            return false;
        }
    }
}
