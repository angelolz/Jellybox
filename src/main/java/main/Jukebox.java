package main;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import commands.*;
import listeners.ButtonListener;

import lombok.Getter;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.UtilClass;

public class Jukebox
{
    //bot setup
    private static long uptime;
    @Getter
    private static CommandClient client;

    //logger
    @Getter
    private static Logger logger;


    public static void main(String[] args)
    {
        //logger
        logger = LoggerFactory.getLogger(Jukebox.class);

        //create builder for adding commands and listeners
        CommandClientBuilder clientBuilder = new CommandClientBuilder();

        //bot config
        clientBuilder.useHelpBuilder(false)
                     .setPrefix(Config.getPrefix())
                     .setOwnerId(Config.getOwnerId())
                     .setActivity(Activity.listening("music! | " + Config.getPrefix() + "help"))
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
            new Queue()
        );

        client = clientBuilder.build();

        try
        {
            //start tracking uptime
            uptime = System.currentTimeMillis();

            //start building bot
            JDABuilder.createDefault(Config.getToken())
                      .setStatus(OnlineStatus.DO_NOT_DISTURB)
                      .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                      .setActivity(Activity.listening("loading!! | " + Config.getPrefix() + "help"))
                      .addEventListeners(client, new ButtonListener())
                      .build();
        }

        catch(Exception e)
        {
            System.out.println("Unable to login with bot token.");
            e.printStackTrace();
        }
    }

    public static String getUptime()
    {
        return UtilClass.convertLongToDaysLength(System.currentTimeMillis() - uptime);
    }
}
