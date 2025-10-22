package music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Getter;
import lombok.Setter;
import main.Jellybox;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import utils.UtilClass;

import java.awt.*;
import java.util.LinkedList;

@Getter
public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;
    private final LinkedList<AudioTrack> queue;
    @Setter
    private VoiceChannel notifChannel;
    @Setter
    private LoopState loopState;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedList<>();
        this.loopState = LoopState.DISABLED;
    }

    public boolean queue(AudioTrack track, User requester) {
        track.setUserData(requester);

        if(!this.player.startTrack(track, true)) {
            queue.offer(track);
            return true;
        }

        else
            return false;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(notifChannel.getGuild());

        if(loopState == LoopState.TRACK)
            musicManager.getScheduler().getQueue().addFirst(track.makeClone());
        else if(loopState == LoopState.QUEUE)
            musicManager.getScheduler().getQueue().addLast(track.makeClone());

        if(endReason.mayStartNext)
            nextTrack();
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException e) {
        EmbedBuilder embed = new EmbedBuilder().setColor(Color.red);
        embed.setTitle("Error!")
             .setDescription("❌ Couldn't play track!")
             .addField("Track", track.getInfo().title, false)
             .addField("Requested By", track.getUserData(User.class).getAsMention(), false);

        notifChannel.sendMessageEmbeds(embed.build()).queue();
        e.printStackTrace();
        Jellybox.getLogger().error("Error occurred when playing track: {}: {}", e.getClass().getName(), e.getMessage());
    }

    public void nextTrack() {
        if(queue.peek() != null) {
            AudioTrack nextTrack = queue.poll();
            player.startTrack(nextTrack, false);

            EmbedBuilder embed = UtilClass.getNowPlayingEmbed(nextTrack);
            notifChannel.sendMessageEmbeds(embed.build()).queue();
        }
    }
}
