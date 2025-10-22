package listeners;

import commands.Help;
import commands.Queue;
import main.Jellybox;
import music.PlayerManager;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class InteractionListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] args = event.getComponentId().split(":");
        if(!event.getUser().getId().equals(args[0]))
            return;

        if(args[1].equalsIgnoreCase("pagination")) {
            switch(args[2].toLowerCase()) {
                case "queue" -> Queue.paginate(event, args);
                case "help" -> Help.getEmbed(event, args[3]);
                default -> Jellybox.getLogger().error("Unknown button interaction: {} | ID: {}", args[2], event.getComponentId());
            }
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        String[] args = event.getComponentId().split(":");
        if(!event.getUser().getId().equals(args[0]))
            return;

        event.getInteraction().deferEdit().queue();
        if(args[1].equalsIgnoreCase("play")) {
            switch(args[2].toLowerCase()) {
                case "track-selection" -> {
                    String trackId = event.getValues().get(0);
                    PlayerManager.getInstance()
                                 .loadAndPlay(event.getGuildChannel(), event.getUser(), "jellyfin://track/" + trackId);
                }
                case "album-selection" -> {
                    String albumId = event.getValues().get(0);
                    PlayerManager.getInstance()
                                 .loadAndPlay(event.getGuildChannel(), event.getUser(), "jellyfin://album/" + albumId);
                    event.getMessage().delete().queue();
                    
                }
                case "artist-selection" -> {
                    String artistId = event.getValues().get(0);
                    PlayerManager.getInstance()
                                 .loadAndPlay(event.getGuildChannel(), event.getUser(), "jellyfin://artist/" + artistId);
                    event.getMessage().delete().queue();
                }
                default -> Jellybox.getLogger().error("Unknown string select interaction: {} | ID: {}", args[2], event.getComponentId());
            }
        }
    }
}