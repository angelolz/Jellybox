package listeners;

import commands.Help;
import commands.Lyrics;
import commands.Queue;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ButtonListener extends ListenerAdapter
{
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event)
    {
        String[] args = event.getComponentId().split(":");
        if(event.getUser().getId().equals(args[0]))
        {
            if(args[1].equalsIgnoreCase("pagination"))
            {
                switch(args[2].toLowerCase())
                {
                    case "queue" -> runQueue(args, event);
                    case "lyrics" -> runLyrics(args, event);
                    case "help" -> runHelp(args, event);
                }
            }
        }
    }

    private void runHelp(String[] args, ButtonInteractionEvent event){
        Help.getEmbed(event, args[3]);
    }

    private void runLyrics(String[] args, ButtonInteractionEvent event)
    {
        switch(args[3].toLowerCase())
        {
            case "left" -> Lyrics.getEmbed(event, Integer.parseInt(args[4]) - 1, args[5]);
            case "right" -> Lyrics.getEmbed(event, Integer.parseInt(args[4]) + 1, args[5]);
        }
    }

    private void runQueue(String[] args, ButtonInteractionEvent event)
    {
        switch(args[3].toLowerCase())
        {
            case "left" -> Queue.getEmbed(event, Integer.parseInt(args[4]) - 1, args[5]);
            case "right" -> Queue.getEmbed(event, Integer.parseInt(args[4]) + 1, args[5]);
            case "refresh" -> Queue.getEmbed(event, Integer.parseInt(args[4]), args[5]);
        }
    }
}
