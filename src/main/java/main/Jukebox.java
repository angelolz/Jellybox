package main;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.wrapper.spotify.SpotifyApi;
import commands.*;
import core.GLA;
import listeners.ScheduledTasks;
import listeners.ButtonListener;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import structure.TwitchApi;
import utils.ConvertLong;
import utils.LyricsFetcher;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Jukebox
{
    //bot setup
    private static final String PREFIX = "+";
    private static final String VERSION = "1.2.2";
    private static long uptime;

    //logger
    private static Logger logger;

    //apis
    private static SpotifyApi spotifyApi;
    private static GLA geniusApi;
    private static TwitchApi twitchApi;

    //Cache
    private static AsyncLoadingCache<String, List<String>> cache; // Cache a list of descriptions

    // Help
    private static Hashtable<String, List<Command>> helpCache;

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

        String twitchClientId = prop.getProperty("twitch_client_id");
        String twitchClientSecret = prop.getProperty("twitch_client_secret");

        //create builder for adding commands and listeners
        CommandClientBuilder client = new CommandClientBuilder();

        // Initialize cache
        cache = Caffeine.newBuilder()
            .maximumSize(20)
            .expireAfterWrite(3, TimeUnit.MINUTES)
            .buildAsync(LyricsFetcher::get);

        //bot config
        client.useHelpBuilder(false);
        client.setPrefix(PREFIX);
        client.setOwnerId(ownerId);
        client.setCoOwnerIds(coOwnerIds[0], coOwnerIds[1]);
        client.setActivity(Activity.listening("music! | +help"));

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
            new Shuffle(),
            new Lyrics(),
            new NowPlaying(),
            new Leave(),
            new Queue()
        );

        //admin/hidden commands
        client.addCommands(
            new Invite(),
            new Admin()
        );

        // help cache
        helpCache = new Hashtable<>();

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

            //build twitch api
            twitchApi = new TwitchApi(twitchClientId, twitchClientSecret);
            getTwitchApi().updateAccessToken();
            logger.info("Finished getting Twitch API refresh token.");

            //start building bot
            JDABuilder.createDefault(token)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.listening("loading!! | !help"))
                .addEventListeners(client.build(), new ButtonListener(), new ScheduledTasks(client.build()))
                .build();
        }

        catch(Exception e)
        {
            System.out.println("Unable to login with bot token.");
            e.printStackTrace();
        }
    }

    public static String getVersion()
    {
        return VERSION;
    }

    public static String getPrefix()
    {
        return PREFIX;
    }

    public static Logger getLogger()
    {
        return logger;
    }

    public static String getUptime()
    {
        return ConvertLong.convertLongToDaysLength(System.currentTimeMillis() - uptime);
    }

    public static SpotifyApi getSpotifyApi() { return spotifyApi; }

    public static GLA getGeniusApi() { return geniusApi; }

    public static TwitchApi getTwitchApi() { return twitchApi; }

    public static AsyncLoadingCache<String, List<String>> getCache() { return cache; }
    public static Hashtable<String, List<Command>> getHelpCache() { return helpCache; }

}
