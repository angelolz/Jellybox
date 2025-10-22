package main;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.UtilClass;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    private static final Logger logger = LoggerFactory.getLogger(Config.class);

    @Getter
    private static String token;
    @Getter
    private static String ownerId;
    @Getter
    private static String prefix;
    @Getter
    private static String version;
    @Getter
    private static String jellyfinUrl;
    @Getter
    private static String jellyfinApiKey;
    @Getter
    private static String jellyfinLibraryId;
    @Getter
    private static String jellyfinUserId;

    static {
        Properties prop = new Properties();
        boolean isDocker = Boolean.parseBoolean(System.getenv("DOCKER"));

        if(!isDocker) {
            try(FileInputStream propFile = new FileInputStream("config.properties")) {
                prop.load(propFile);
                logger.info("Loaded configuration from config.properties");
            }
            catch(IOException e) {
                logger.warn("Could not load config.properties, falling back to environment variables");
            }
        }
        else {
            logger.info("Running inside Docker — loading configuration from environment variables");
        }

        token = getConfigValue(isDocker, "BOT_TOKEN", "bot_token", prop);
        ownerId = getConfigValue(isDocker, "OWNER_ID", "owner_id", prop);
        prefix = getConfigValue(isDocker, "PREFIX", "prefix", prop, "+");
        version = getConfigValue(isDocker, "VERSION", "version", prop, "2.0.1");
        jellyfinUrl = getConfigValue(isDocker, "JELLYFIN_URL", "jellyfin_url", prop);
        jellyfinApiKey = getConfigValue(isDocker, "JELLYFIN_API_KEY", "jellyfin_api_key", prop);
        jellyfinUserId = getConfigValue(isDocker, "JELLYFIN_USER_ID", "jellyfin_user_id", prop);
        jellyfinLibraryId = getConfigValue(isDocker, "JELLYFIN_LIBRARY_ID", "jellyfin_library_id", prop);

        if(UtilClass.isNullOrEmpty(token) || UtilClass.isNullOrEmpty(ownerId)) {
            logger.error("Missing required configuration values. Exiting.");
            System.exit(1);
        }
    }

    private static String getConfigValue(boolean isDocker, String envKey, String propKey, Properties prop) {
        return getConfigValue(isDocker, envKey, propKey, prop, null);
    }

    private static String getConfigValue(boolean isDocker, String envKey, String propKey, Properties prop,
                                         String defaultValue) {
        String val;

        if(!isDocker) {
            val = prop.getProperty(propKey);
            if(val != null)
                return val;
        }

        val = System.getenv(envKey);
        if(val != null)
            return val;

        return defaultValue;
    }

    private Config() { }
}