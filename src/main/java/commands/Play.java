package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.SneakyThrows;
import main.Jellybox;
import music.PlayerManager;
import music.sources.jellyfin.JellyfinApi;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import structure.jellyfin.JellyfinAlbum;
import structure.jellyfin.JellyfinArtist;
import structure.jellyfin.JellyfinTrack;
import utils.Statics;
import utils.ThrowingFunction;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class Play extends Command {

    private static final String TRACK = "track";
    private static final String ARTIST = "artist";
    private static final String ALBUM = "album";
    private static final String SONG = "song";

    public Play() {
        this.name = "play";
        this.aliases = new String[]{ "p" };
        this.arguments = "[url or query]";
        this.help = "Plays a track from a URL or search query.";
        this.cooldown = 3;

        this.category = new Category("Player");
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        GuildVoiceState botState = commandEvent.getSelfMember().getVoiceState();
        GuildVoiceState userState = commandEvent.getMember().getVoiceState();

        if(userState == null || !userState.inAudioChannel()) {
            commandEvent.replyError("You need to be in a voice channel to use this command!");
            return;
        }

        var manager = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild());
        var player = manager.getScheduler().getPlayer();
        var queue = manager.getScheduler().getQueue();

        if(!joinIfNeeded(commandEvent, botState, userState))
            return;

        if(commandEvent.getArgs().isEmpty())
            unpauseBot(commandEvent, player, queue);
        else
            handleQuery(commandEvent, player);
    }

    private boolean joinIfNeeded(CommandEvent event, GuildVoiceState self, GuildVoiceState user) {
        if((self != null && self.inAudioChannel()) && (user.getChannel() != null && !user.getChannel().equals(self.getChannel()))) {
            event.replyError("I'm already in another voice channel!");
            return false;
        }

        if(self != null && !self.inAudioChannel())
            Join.joinVoiceChannel(event, user);
        return true;
    }

    private static void unpauseBot(CommandEvent commandEvent, AudioPlayer player, LinkedList<AudioTrack> queue) {
        if(player.isPaused()) {
            player.setPaused(false);
            commandEvent.reply(":arrow_forward: | Resumed playback!");
        }

        else {
            if(player.getPlayingTrack() != null) {
                commandEvent.replyError("There's already a track playing! Add one to the queue using a query or URL.");
                return;
            }

            if(queue.isEmpty()) {
                commandEvent.replyError("There's nothing to play! Add a track with a search query or URL.");
                return;
            }

            player.playTrack(queue.poll());
            commandEvent.reply(":arrow_forward: | Resumed playback!");
        }
    }

    @SneakyThrows
    private void handleQuery(CommandEvent event, AudioPlayer player) {
        String[] parts = event.getArgs().split(":", 2);
        String type = parts[0].toLowerCase();
        String query = (parts.length > 1) ? parts[1] : event.getArgs();

        switch(type) {
            case SONG, TRACK ->
                handleResults(event, player, query, JellyfinApi::searchTracks, this::describeTracks, TRACK);
            case ALBUM -> handleResults(event, player, query, JellyfinApi::searchAlbums, this::describeAlbums, ALBUM);
            case ARTIST ->
                handleResults(event, player, query, JellyfinApi::searchArtists, this::describeArtists, ARTIST);
            default ->
                handleResults(event, player, event.getArgs(), JellyfinApi::searchTracks, this::describeTracks, TRACK);
        }
    }

    private <T> void handleResults(CommandEvent event, AudioPlayer player, String query, ThrowingFunction<String,
        List<T>, IOException> searchFunc, Function<List<T>, String> describeFunc, String type) {
        try {

            List<T> results = searchFunc.apply(query);
            if(results.isEmpty()) {
                event.reply("❌ No " + type + "s found with your query!");
                return;
            }

            if(results.size() == 1) {
                playSingle(event, player, results.get(0), type);
                return;
            }

            EmbedBuilder embed = new EmbedBuilder().setColor(Statics.EMBED_COLOR)
                                                   .setTitle("Multiple " + type + "s found!")
                                                   .setDescription("Please select a " + type + " to play:\n\n" + describeFunc.apply(results));

            StringSelectMenu.Builder menu = StringSelectMenu.create(event.getAuthor()
                                                                         .getId() + ":play:" + type + "-selection");
            for(int i = 0; i < Math.min(10, results.size()); i++) {
                T item = results.get(i);
                String label;
                String id;

                if(item instanceof JellyfinTrack t) {
                    label = t.getArtist() + " - " + t.getTrackName();
                    id = t.getId();
                }
                else if(item instanceof JellyfinAlbum a) {
                    label = a.getAlbumArtist() + " - " + a.getAlbumName();
                    id = a.getId();
                }
                else if(item instanceof JellyfinArtist a) {
                    label = a.getName();
                    id = a.getId();
                }
                else {
                    continue;
                }

                menu.addOption(label, id);
            }

            event.getChannel().sendMessageEmbeds(embed.build()).setComponents(ActionRow.of(menu.build())).queue();
        }

        catch(IOException e) {
            event.replyError("Sorry, there was an error looking up your query.");
            Jellybox.getLogger().error("Error looking up query", e);
        }
    }

    private <T> void playSingle(CommandEvent event, AudioPlayer player, T item, String type) {
        if(player.isPaused()) {
            event.reply(":pause_button: | The player is still paused! Type `!p` or `!play` to resume.");
            return;
        }

        String id = switch(type) {
            case TRACK -> ((JellyfinTrack) item).getId();
            case ALBUM -> ((JellyfinAlbum) item).getId();
            case ARTIST -> ((JellyfinArtist) item).getId();
            default -> null;
        };

        if(id != null)
            PlayerManager.getInstance()
                         .loadAndPlay(event.getGuildChannel(), event.getAuthor(), "jellyfin://" + type + "/" + id);
    }

    private String describeTracks(List<JellyfinTrack> list) {
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < Math.min(10, list.size()); i++) {
            JellyfinTrack t = list.get(i);
            b.append(i + 1).append(". ").append(t.getArtist()).append(" - ").append(t.getTrackName()).append("\n");
        }
        return b.toString();
    }

    private String describeAlbums(List<JellyfinAlbum> list) {
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < Math.min(10, list.size()); i++) {
            JellyfinAlbum a = list.get(i);
            b.append(i + 1)
             .append(". ")
             .append(a.getAlbumArtist())
             .append(" - ")
             .append(a.getAlbumName())
             .append(" (")
             .append(a.getYear())
             .append(")\n");
        }
        return b.toString();
    }

    private String describeArtists(List<JellyfinArtist> list) {
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < Math.min(10, list.size()); i++) {
            b.append(i + 1).append(". ").append(list.get(i).getName()).append("\n");
        }
        return b.toString();
    }
}
