package listeners;

import commands.Help;
import commands.Queue;
import main.Jukebox;
import music.PlayerManager;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ButtonListener extends ListenerAdapter
{
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event)
    {
        String[] args = event.getComponentId().split(":");
        if(!event.getUser().getId().equals(args[0])) return;

        if(args[1].equalsIgnoreCase("pagination"))
        {
            switch(args[2].toLowerCase())
            {
                case "queue" -> Queue.paginate(event, args);
                case "help" -> Help.getEmbed(event, args[3]);
                default -> Jukebox.getLogger().error("Unknown action: {} | ID: {}", args[2], event.getComponentId());
            }
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event)
    {
        String[] args = event.getComponentId().split(":");
        if(!event.getUser().getId().equals(args[0])) return;

        event.getInteraction().deferEdit().queue();
        if(args[1].equalsIgnoreCase("play"))
        {
            if(args[2].equalsIgnoreCase("track-selection"))
            {
                String trackId = event.getValues().get(0);
                PlayerManager.getInstance().loadAndPlay(event.getChannel().asTextChannel(), event.getUser(), "jellyfin://" + trackId);
                event.getMessage().delete().queue();
            }
        }
    }
}