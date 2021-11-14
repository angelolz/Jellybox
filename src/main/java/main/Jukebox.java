package main;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.zaxxer.hikari.HikariDataSource;
import commands.*;
import core.GLA;
import listeners.ScheduledTasks;
import listeners.ButtonListener;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.LyricsFetcher;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Jukebox
{
    //bot setup
    private static final String prefix = "!";
    private static final String version = "1.0";
    private static long uptime;

    //logger
    private static Logger logger;

    //mysql database (might need it?)
    private static HikariDataSource ds;

    //apis
    private static SpotifyApi spotifyApi;
    private static GLA geniusApi;

    //Cache
    private static AsyncLoadingCache<String, List<String>> cache; // Cache a list of descriptions

    public static void main(String[] args) throws IOException, IllegalArgumentException
    {
        //logger
        logger = LoggerFactory.getLogger(Jukebox.class);

        //gets tokens and IDs of developers
        Properties prop = new Properties();
        FileInputStream propFile = new FileInputStream("config.properties");
        prop.load(propFile);

        String token = prop.getProperty("bot_token");
        String ownerId = prop.getProperty("angel_id");
        String[] coOwnerIds = new String[] {prop.getProperty("andrew_id"), prop.getProperty("daniel_id")};

        String spClientId = prop.getProperty("spotify_client_id");
        String spClientSecret = prop.getProperty("spotify_client_secret");

        String geniusId = prop.getProperty("gla_id");
        String geniusToken = prop.getProperty("gla_access_token");

        //create builder for adding commands and listeners
        CommandClientBuilder client = new CommandClientBuilder();

        // Initialize cache
        cache = Caffeine.newBuilder()
                .maximumSize(20)
                .expireAfterWrite(3, TimeUnit.MINUTES)
                .buildAsync(LyricsFetcher::get);

        //bot config
        client.useHelpBuilder(false);
        client.setPrefix(prefix);
        client.setOwnerId(ownerId);
        client.setCoOwnerIds(coOwnerIds[0], coOwnerIds[1]);
        client.setActivity(Activity.listening("music!"));

        //add commands
        client.addCommands(
            new Help(),
            new Ping(),
            new Join(),
            new Play(),
            new Stop(),
            new Pause(),
            new Skip(),
            new Repeat(),
            new Lyrics(),
            new NowPlaying(),
            new Leave(),
            new Queue()
        );

        try
        {
            //start tracking uptime
            uptime = System.currentTimeMillis();

            //build genius api
            geniusApi = new GLA(geniusId, geniusToken);
            logger.info("Finished loading Genius API.");

            //build spotify api
            spotifyApi = new SpotifyApi.Builder()
                    .setClientId(spClientId)
                    .setClientSecret(spClientSecret)
                    .build();

            logger.info("Finished loading Spotify API.");

            //start building bot
            JDABuilder.createDefault(token)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.listening("loading!! | !help"))
                .addEventListeners(client.build(), new ButtonListener())
                .build();

            //run scheduled tasks
            ScheduledTasks.init();
        }

        catch(LoginException e)
        {
            System.out.println("Unable to login with bot token.");
            e.printStackTrace();
        }
    }

    public static String getVersion()
    {
        return version;
    }

    public static String getPrefix()
    {
        return prefix;
    }

    public static Logger getLogger()
    {
        return logger;
    }

    public static long getUptime()
    {
        return uptime;
    }

    public static SpotifyApi getSpotifyApi() { return spotifyApi; }

    public static GLA getGeniusApi() { return geniusApi; }

    public static AsyncLoadingCache<String, List<String>> getCache() { return cache; }
}
