package music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import main.Jellybox;
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
                 .addField("Time before track plays", String.format("#%s - `[%s]` left", queue.size(),
                     UtilClass.convertLongToTrackTime(totalQueueLength)), true);
        }

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    @Override
    public void playlistLoaded(AudioPlaylist audioPlaylist) {
        long playlistDuration = 0;
        int tracksAdded = 0;
        for(AudioTrack track : audioPlaylist.getTracks()) {
            if(!guildMusicManager.getScheduler().queue(track, requester))
                guildMusicManager.getNotifChannel().sendMessageEmbeds(UtilClass.getNowPlayingEmbed(track).build()).queue();
            tracksAdded++;
            playlistDuration += track.getDuration();
        }

        EmbedBuilder embed = new EmbedBuilder().setColor(Statics.EMBED_COLOR)
                                               .setTitle("Added to Queue!")
                                               .setDescription(String.format("🎶 Added **%d** tracks from **%s** to the queue!", tracksAdded, audioPlaylist.getName()))
                                               .addField("Total time added:",
                                                   UtilClass.convertLongToTrackTime(playlistDuration), true)
                                               .addField("Requested by:", requester.getAsMention(), true);

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    @Override
    public void noMatches() {
        channel.sendMessage("❌ | Sorry, we couldn't find a matching result for your request!").queue();
    }

    @Override
    public void loadFailed(FriendlyException e) {
        channel.sendMessage("❌ | There was an error trying to play your track.").queue();
        Jellybox.getLogger().error("Error occurred when playing track: {}: {}", e.getClass().getName(), e.getMessage());
        e.printStackTrace();
    }

    private long calculateTotalQueueLength(AudioPlayer player, LinkedList<AudioTrack> queue) {
        long totalQueueLength = 0;
        totalQueueLength += player.getPlayingTrack().getDuration() - player.getPlayingTrack().getPosition();

        for(AudioTrack track : queue)
            totalQueueLength += track.getDuration();

        return totalQueueLength;
    }
}
