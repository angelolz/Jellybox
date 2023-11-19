package listeners;

import commands.Help;
import commands.Queue;
import main.Jukebox;
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
                    case "queue" -> Queue.paginate(event, args);
                    case "help" -> Help.getEmbed(event, args[3]);
                    default ->
                        Jukebox.getLogger().error("Unknown action: {} | ID: {}", args[2], event.getComponentId());
                }
            }
        }
    }
}
