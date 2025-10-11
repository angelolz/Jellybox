package music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import main.Jukebox;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import utils.Statics;
import utils.UtilClass;

import java.util.LinkedList;

public class SourceAudioLoadResultHandler implements AudioLoadResultHandler {

    private final GuildMusicManager guildMusicManager;
    private final User requester;
    private final TextChannel channel;

    public SourceAudioLoadResultHandler(GuildMusicManager guildMusicManager, User requester, TextChannel channel) {
        this.guildMusicManager = guildMusicManager;
        this.requester = requester;
        this.channel = channel;
    }

    @Override
    public void trackLoaded(AudioTrack audioTrack) {
        AudioTrackInfo trackInfo = audioTrack.getInfo();
        TrackScheduler scheduler = guildMusicManager.getScheduler();
        AudioPlayer player = scheduler.getPlayer();
        EmbedBuilder embed = new EmbedBuilder().setColor(Statics.EMBED_COLOR);

        if(!scheduler.queue(audioTrack, requester))
            embed = UtilClass.getNowPlayingEmbed(audioTrack);
        else {
            LinkedList<AudioTrack> queue = scheduler.getQueue();
            long totalQueueLength = calculateTotalQueueLength(player, queue);

            embed.setTitle("Added to Queue!")
                 .setThumbnail(trackInfo.artworkUrl)
                 .setDescription(UtilClass.getTrackInfoForEmbed(trackInfo));

            embed.addField("Length", UtilClass.convertLongToTrackTime(trackInfo.length), true)
                 .addField("Requested by:", requester.getAsMention(), true)
                 .addField("Time before track plays", String.format("#%s - `[%s]` left", queue.size(), UtilClass.convertLongToTrackTime(totalQueueLength)), true);
        }

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    @Override
    public void playlistLoaded(AudioPlaylist audioPlaylist) {
        //        if(audioPlaylist.isSearchResult())
        //        {
        //            trackLoaded(audioPlaylist.getTracks().get(0));
        //            return;
        //        }
        //
        //        if(!channel.getGuild().getAudioManager().isConnected())
        //        {
        //            channel.sendMessageFormat("❌ | Playlist **%d** was not loaded due to the bot not being in a
        //            voice channel.", audioPlaylist.getName()).queue();
        //            return;
        //        }
        //
        //        long playlistDuration = 0;
        //        int tracksAdded = 0;
        //        boolean limitReached = false;
        //        for(AudioTrack track : audioPlaylist.getTracks())
        //        {
        //            if(guildMusicManager.getScheduler().getQueue().size() < Statics.MAX_QUEUE_ITEMS)
        //            {
        //                guildMusicManager.getScheduler().queue(track, requester);
        //                tracksAdded++;
        //                playlistDuration += track.getDuration();
        //            }
        //
        //            else
        //            {
        //                limitReached = true;
        //                break;
        //            }
        //        }
        //
        //        embed.setColor(Statics.EMBED_COLOR)
        //             .setDescription(String.format("🎶 Added **%d** tracks to the queue from the playlist **%s**!",
        //             tracksAdded, audioPlaylist.getName()))
        //             .addField("Total time added:", UtilClass.convertLongToTrackTime(playlistDuration), true)
        //             .addField("Requested by:", requester.getAsMention(), true);
        //
        //        if(limitReached)
        //            embed.appendDescription(String.format("%n%nOnly the first **%d** tracks of the playlist were
        //            added to the queue due to the maximum queue size of %d tracks being reached.", tracksAdded,
        //            Statics.MAX_QUEUE_ITEMS));
        //
        //        channel.sendMessageEmbeds(embed.build()).queue();
    }

    @Override
    public void noMatches() {
        channel.sendMessage("❌ | Sorry, we couldn't find a matching result for your request!").queue();
    }

    @Override
    public void loadFailed(FriendlyException e) {
        channel.sendMessage("❌ | There was an error trying to play your track.").queue();
        e.printStackTrace();
        Jukebox.getLogger().error("Error occurred when playing track: {}: {}", e.getClass().getName(), e.getMessage());
    }

    private long calculateTotalQueueLength(AudioPlayer player, LinkedList<AudioTrack> queue) {
        long totalQueueLength = 0;
        totalQueueLength += player.getPlayingTrack().getDuration() - player.getPlayingTrack().getPosition();

        for(AudioTrack track : queue)
            totalQueueLength += track.getDuration();

        return totalQueueLength;
    }
}
