package main;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.zaxxer.hikari.HikariDataSource;
import commands.*;
import listeners.ButtonListener;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

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

        //create builder for adding commands and listeners
        CommandClientBuilder client = new CommandClientBuilder();

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
            new Lyrics(),
            new NowPlaying(),
            new Leave(),
            new Queue()
        );

        try
        {
            //start tracking uptime
            uptime = System.currentTimeMillis();

            //start building bot
            JDABuilder.createDefault(token)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.listening("loading!! | !help"))
                .addEventListeners(client.build(), new ButtonListener())
                .build();
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
}
