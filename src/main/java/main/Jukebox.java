package main;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import commands.*;
import listeners.ScheduledTasks;
import listeners.ButtonListener;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import structure.TwitchApi;
import utils.UtilClass;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Jukebox
{
    //bot setup
    private static final String PREFIX = "+";
    private static final String VERSION = "1.3.3";
    private static long uptime;
    private static CommandClient client;

    //logger
    private static Logger logger;

    //apis
    private static SpotifyApi spotifyApi;
    private static TwitchApi twitchApi;


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

        String spClientId = prop.getProperty("spotify_client_id");
        String spClientSecret = prop.getProperty("spotify_client_secret");

        String twitchClientId = prop.getProperty("twitch_client_id");
        String twitchClientSecret = prop.getProperty("twitch_client_secret");

        //create builder for adding commands and listeners
        CommandClientBuilder clientBuilder = new CommandClientBuilder();

        //bot config
        clientBuilder.useHelpBuilder(false)
                     .setPrefix(PREFIX)
                     .setOwnerId(ownerId)
                     .setActivity(Activity.listening("music! | +help"))
                     .setEmojis("✅ | ", "⚠️ | ", "❌ | ");

        //add commands
        clientBuilder.addCommands(
            new Help(),
            new Ping(),
            new Join(),
            new Play(),
            new Stop(),
            new Pause(),
            new Skip(),
            new Repeat(),
            new Shuffle(),
            new NowPlaying(),
            new Leave(),
            new Queue(),
            new Analyze()
        );

        //admin/hidden commands
        clientBuilder.addCommands(
            new Invite(),
            new Admin()
        );

        client = clientBuilder.build();

        try
        {
            //start tracking uptime
            uptime = System.currentTimeMillis();

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
                      .addEventListeners(client, new ButtonListener(), new ScheduledTasks())
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

    public static CommandClient getClient()
    {
        return client;
    }

    public static String getUptime()
    {
        return UtilClass.convertLongToDaysLength(System.currentTimeMillis() - uptime);
    }

    public static SpotifyApi getSpotifyApi() { return spotifyApi; }

    public static TwitchApi getTwitchApi() { return twitchApi; }

}
