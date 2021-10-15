package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import core.GLA;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Lyrics extends Command
{

    private GLA lyricsGetter;

    public Lyrics()
    {
        this.name = "lyrics";
        this.help = "Returns lyrics for any song";
        this.cooldown = 3;
        this.arguments = "!lyrics <song-name> - Returns <song-name>'s lyrics";

        // Set up for Genius Lyrics API
        try
        {
            // Set up for GLA
            Properties prop = new Properties();
            FileInputStream propFile = new FileInputStream("config.properties");
            prop.load(propFile);
            lyricsGetter = new GLA(prop.getProperty("gla_id"), prop.getProperty("gla_access_token"));
        }
        catch (IOException e)
        {
            System.out.println("Config file error!");
        }
    }
    @Override
    protected void execute(CommandEvent event)
    {
        MessageChannel channel = event.getChannel();
        String[] lyrics = lyricsGetter.search("Lose Yourself").get(0).getText().split(System.lineSeparator());
        StringBuilder sb = new StringBuilder();
        System.out.println("Start");
        for(String line: lyrics)
        {
            if((sb.length() + line.length()) > 1900)
            {
                channel.sendMessage(sb.toString()).queue();
                sb = new StringBuilder();
            }
            else
            {
                sb.append(line).append("\n");
            }
        }

        if(!sb.toString().equals("")){
            channel.sendMessage(sb).queue();
        }
    }


}
