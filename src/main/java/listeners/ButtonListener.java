package listeners;

import commands.Queue;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Locale;

public class ButtonListener extends ListenerAdapter {
    @Override
    public void onButtonClick(ButtonClickEvent event) {
        //ex: 189690228292845568:pagination:queue:left:2:897901180330590218
        String[] args = event.getComponentId().split(":");
        if(event.getUser().getId().equals(args[0]))
        {
            if(args[1].equalsIgnoreCase("pagination"))
            {
                switch (args[2].toLowerCase())
                {
                    case "queue":
                        runQueue(args, event);
                        break;
                    case "lyrics":
                        break;
                }
            }
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
