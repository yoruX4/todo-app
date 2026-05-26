package com.todoapp.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralised configuration loader.
 *
 * Reads sensitive values (DB credentials, etc.) from:
 *   1. A local .env file (development convenience – never committed to Git)
 *   2. Real OS environment variables (production / CI)
 *
 * NO credentials are hard-coded here.
 */
public class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static final Dotenv dotenv;

    static {
        Dotenv loaded;
        try {
            // ignoreIfMissing() lets the app run without a .env file when
            // variables are supplied via real environment variables instead.
            loaded = Dotenv.configure()
                           .ignoreIfMissing()
                           .load();
            logger.info("Configuration loaded (.env file found or OS env used).");
        } catch (DotenvException e) {
            logger.warn("Could not load .env file – falling back to OS environment variables.");
            loaded = Dotenv.configure().ignoreIfMissing().load();
        }
        dotenv = loaded;
    }

    // -----------------------------------------------------------------------
    // Database settings
    // -----------------------------------------------------------------------

    public static String getDbHost() {
        return get("DB_HOST", "localhost");
    }

    public static String getDbPort() {
        return get("DB_PORT", "3306");
    }

    public static String getDbName() {
        return get("DB_NAME", "todoapp");
    }

    public static String getDbUser() {
        return get("DB_USER", "");
    }

    public static String getDbPassword() {
        return get("DB_PASSWORD", "");
    }

    // -----------------------------------------------------------------------
    // Connection pool tuning (optional overrides)
    // -----------------------------------------------------------------------

    public static int getPoolMaxSize() {
        return Integer.parseInt(get("DB_POOL_MAX_SIZE", "10"));
    }

    public static int getPoolMinIdle() {
        return Integer.parseInt(get("DB_POOL_MIN_IDLE", "2"));
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    /**
     * Retrieves a configuration value, preferring the OS environment variable
     * over the .env file entry, and falling back to {@code defaultValue}.
     */
    private static String get(String key, String defaultValue) {
        // System env takes priority (allows Docker / CI overrides)
        String sysVal = System.getenv(key);
        if (sysVal != null && !sysVal.isBlank()) {
            return sysVal;
        }
        String dotVal = dotenv.get(key);
        return (dotVal != null && !dotVal.isBlank()) ? dotVal : defaultValue;
    }
}
