package listeners;

import commands.Lyrics;
import commands.Queue;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ButtonListener extends ListenerAdapter
{
    @Override
    public void onButtonClick(ButtonClickEvent event)
    {
        String[] args = event.getComponentId().split(":");
        if(event.getUser().getId().equals(args[0]))
        {
            if(args[1].equalsIgnoreCase("pagination"))
            {
                switch (args[2].toLowerCase())
                {
                    //ex: 189690228292845568:pagination:queue:left:2:897901180330590218
                    case "queue":
                        runQueue(args, event);
                        break;
                    //ex: 189690228292845568:pagination:lyrics:right:1:lose yourself
                    case "lyrics":
                        runLyrics(args, event);
                        break;
                }
            }
        }
    }

    private void runLyrics(String[] args, ButtonClickEvent event)
    {
        switch (args[3].toLowerCase())
        {
            case "left":
                Lyrics.getEmbed(event, Integer.parseInt(args[4])-1, args[5]);
                break;
            case "right":
                Lyrics.getEmbed(event, Integer.parseInt(args[4])+1, args[5]);
                break;
        }
    }

    private void runQueue(String[] args, ButtonClickEvent event)
    {
        switch (args[3].toLowerCase())
        {
            case "left":
                Queue.getEmbed(event, Integer.parseInt(args[4])-1, args[5]);
                break;
            case "right":
                Queue.getEmbed(event, Integer.parseInt(args[4])+1, args[5]);
                break;
            case "refresh":
                Queue.getEmbed(event, Integer.parseInt(args[4]), args[5]);
        }
    }
}
