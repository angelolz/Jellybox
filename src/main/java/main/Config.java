package main;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        try {
            FileInputStream propFile = new FileInputStream("config.properties");
            prop.load(propFile);
        }
        catch(IOException e) {
            logger.error("Could not load config.properties", e);
            System.exit(1);
        }

        token = prop.getProperty("bot_token");
        ownerId = prop.getProperty("owner_id");
        prefix = prop.getProperty("prefix", "+");
        version = prop.getProperty("version", "2.0.0");
        jellyfinUrl = prop.getProperty("jellyfin_url");
        jellyfinApiKey = prop.getProperty("jellyfin_api_key");
        jellyfinUserId = prop.getProperty("jellyfin_user_id");
        jellyfinLibraryId = prop.getProperty("jellyfin_library_id");
    }

    private Config() {
        // This class should not be instantiated.
    }
}
