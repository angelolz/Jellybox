package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Play extends Command {

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
        GuildVoiceState selfVoiceState = commandEvent.getSelfMember().getVoiceState();
        GuildVoiceState userVoiceState = commandEvent.getMember().getVoiceState();

        if(!userVoiceState.inAudioChannel()) {
            commandEvent.replyError("You need to be in a voice channel to use this command!");
            return;
        }

        AudioPlayer player = PlayerManager.getInstance()
                                          .getMusicManager(commandEvent.getGuild())
                                          .getScheduler()
                                          .getPlayer();
        LinkedList<AudioTrack> queue = PlayerManager.getInstance()
                                                    .getMusicManager(commandEvent.getGuild())
                                                    .getScheduler()
                                                    .getQueue();

        if(!botJoinedVoiceChannel(commandEvent, selfVoiceState, userVoiceState))
            return;

        try {
            if(commandEvent.getArgs().isEmpty())
                unpauseBot(commandEvent, player, queue);
            else
                findResults(commandEvent, player);
        }

        catch(IOException e) {
            commandEvent.replyError("Sorry, there was an error looking up your query.");
            Jellybox.getLogger().error("Error looking up query", e);
        }
    }

    private boolean botJoinedVoiceChannel(CommandEvent commandEvent, GuildVoiceState selfVoiceState,
                                          GuildVoiceState userVoiceState) {
        if(selfVoiceState.inAudioChannel() && !userVoiceState.getChannel().equals(selfVoiceState.getChannel().asVoiceChannel())) {
            commandEvent.replyError("I'm already in another voice channel!");
            return false;
        }

        if(!selfVoiceState.inAudioChannel())
            Join.joinVoiceChannel(commandEvent, userVoiceState);

        return true;
    }

    private static void unpauseBot(CommandEvent commandEvent, AudioPlayer player, LinkedList<AudioTrack> queue) {
        if(player.isPaused()) {
            player.setPaused(false);
            commandEvent.reply(":arrow_forward: | Resumed playback!");
        }

        else {
            if(player.getPlayingTrack() != null) {
                commandEvent.replyError("There's already a track playing! If you want to add a track to the queue, " + "please give me a search query or URL!");
                return;
            }

            if(queue.isEmpty()) {
                commandEvent.replyError("There's nothing to play because the queue is empty! Add a track by giving a "
                    + "search query or URL.");
                return;
            }

            player.playTrack(queue.poll());
            commandEvent.reply(":arrow_forward: | Resumed playback!");
        }
    }

    private static void findResults(CommandEvent commandEvent, AudioPlayer player) throws IOException {
        String[] query = commandEvent.getArgs().split(":", 2);

        switch(query[0].toLowerCase()) {
            case "song", "track" -> findTracks(commandEvent, player, query[1]);
            case "album" -> findAlbums(commandEvent, player, query[1]);
            case "artist" -> findArtists(commandEvent, player, query[1]);
            default -> findTracks(commandEvent, player, commandEvent.getArgs());
        }
    }

    private static void findTracks(CommandEvent commandEvent, AudioPlayer player, String query) throws IOException {
        List<JellyfinTrack> results = JellyfinApi.searchTracks(query);
        if(results.isEmpty()) {
            commandEvent.reply("❌ No tracks found with your query!");
            return;
        }

        if(results.size() > 1) {
            EmbedBuilder embed = new EmbedBuilder().setColor(Statics.EMBED_COLOR)
                                                   .setTitle("Multiple tracks found!")
                                                   .setDescription("Please select a track to play:\n\n" + getTracksForDescription(results));

            StringSelectMenu.Builder menu = StringSelectMenu.create(commandEvent.getAuthor()
                                                                                .getId() + ":play:track-selection");
            for(int i = 0; i < Math.min(10, results.size()); i++) {
                JellyfinTrack track = results.get(i);
                menu.addOption(track.getArtist() + " - " + track.getTrackName(), track.getId());
            }

            commandEvent.getChannel()
                        .sendMessageEmbeds(embed.build())
                        .setComponents(ActionRow.of(menu.build()))
                        .queue();
        }
        else {
            JellyfinTrack track = results.get(0);

            if(player.isPaused())
                commandEvent.reply(":pause_button: | The player is still paused! If you want to resume playback, " +
                    "then" + " type `!p` or `!play`!");

            PlayerManager.getInstance()
                         .loadAndPlay(commandEvent.getTextChannel(), commandEvent.getAuthor(),
                             "jellyfin://track/" + track.getId());
        }
    }

    private static void findAlbums(CommandEvent commandEvent, AudioPlayer player, String query) throws IOException {
        List<JellyfinAlbum> results = JellyfinApi.searchAlbums(query);
        if(results.isEmpty()) {
            commandEvent.reply("❌ No albums found with your query!");
            return;
        }

        if(results.size() > 1) {
            EmbedBuilder embed = new EmbedBuilder().setColor(Statics.EMBED_COLOR)
                                                   .setTitle("Multiple albums found!")
                                                   .setDescription("Please select an album to play:\n\n" + getAlbumsForDescription(results));

            StringSelectMenu.Builder menu = StringSelectMenu.create(commandEvent.getAuthor()
                                                                                .getId() + ":play:album-selection");
            for(int i = 0; i < Math.min(10, results.size()); i++) {
                JellyfinAlbum album = results.get(i);
                menu.addOption(album.getAlbumArtist() + " - " + album.getAlbumName(), album.getId());
            }

            commandEvent.getChannel()
                        .sendMessageEmbeds(embed.build())
                        .setComponents(ActionRow.of(menu.build()))
                        .queue();
        }
        else {
            JellyfinAlbum album = results.get(0);

            if(player.isPaused())
                commandEvent.reply(":pause_button: | The player is still paused! If you want to resume playback, " +
                    "then" + " type `!p` or `!play`!");

            PlayerManager.getInstance()
                         .loadAndPlay(commandEvent.getTextChannel(), commandEvent.getAuthor(),
                             "jellyfin://album/" + album.getId());
        }
    }

    private static void findArtists(CommandEvent commandEvent, AudioPlayer player, String query) throws IOException {
        List<JellyfinArtist> results = JellyfinApi.searchArtists(query);
        if(results.isEmpty()) {
            commandEvent.reply("❌ No albums found with your query!");
            return;
        }

        if(results.size() > 1) {
            EmbedBuilder embed = new EmbedBuilder().setColor(Statics.EMBED_COLOR)
                                                   .setTitle("Multiple artists found!")
                                                   .setDescription("Please select an artist to play from:\n\n" + getArtistForDescription(results));

            StringSelectMenu.Builder menu = StringSelectMenu.create(commandEvent.getAuthor()
                                                                                .getId() + ":play:artist-selection");
            for(int i = 0; i < Math.min(10, results.size()); i++) {
                JellyfinArtist artist = results.get(i);
                menu.addOption(artist.getName(), artist.getId());
            }

            commandEvent.getChannel()
                        .sendMessageEmbeds(embed.build())
                        .setComponents(ActionRow.of(menu.build()))
                        .queue();
        }
        else {
            JellyfinArtist artist = results.get(0);

            if(player.isPaused())
                commandEvent.reply(":pause_button: | The player is still paused! If you want to resume playback, " +
                    "then" + " type `!p` or `!play`!");

            PlayerManager.getInstance()
                         .loadAndPlay(commandEvent.getTextChannel(), commandEvent.getAuthor(),
                             "jellyfin://artist/" + artist.getId());
        }
    }

    private static String getTracksForDescription(List<JellyfinTrack> tracks) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < Math.min(10, tracks.size()); i++) {
            builder.append(i + 1)
                   .append(". ")
                   .append(tracks.get(i).getArtist())
                   .append(" - ")
                   .append(tracks.get(i).getTrackName())
                   .append("\n");
        }

        return builder.toString();
    }

    private static String getAlbumsForDescription(List<JellyfinAlbum> albums) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < Math.min(10, albums.size()); i++) {
            builder.append(i + 1)
                   .append(". ")
                   .append(albums.get(i).getAlbumArtist())
                   .append(" - ")
                   .append(albums.get(i).getAlbumName())
                   .append(" (")
                   .append(albums.get(i).getYear())
                   .append(")\n");
        }

        return builder.toString();
    }

    private static String getArtistForDescription(List<JellyfinArtist> artists) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < Math.min(10, artists.size()); i++) {
            builder.append(i + 1).append(". ").append(artists.get(i).getName()).append("\n");
        }

        return builder.toString();
    }
}
