package music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import structure.MusicTrack;
import utils.ConvertLong;

import java.util.LinkedList;

public class TrackScheduler extends AudioEventAdapter
{
    public final AudioPlayer player;
    public final LinkedList<MusicTrack> queue;

    public TrackScheduler(AudioPlayer player)
    {
         this.player = player;
         this.queue = new LinkedList<>();
    }

    public boolean queue(AudioTrack track, User requester, Guild guild)
    {
        if(!this.player.startTrack(track, true))
        {
            MusicTrack requestTrack = new MusicTrack(track, requester, guild);
            queue.offer(requestTrack);
            return true;
        }

        else
            return false;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason)
    {
        if(endReason.mayStartNext) nextTrack();
    }

    public AudioPlayer getPlayer()
    {
        return player;
    }

    public LinkedList<MusicTrack> getQueue()
    {
        return queue;
    }

    private void nextTrack()
    {
        MusicTrack nextTrack = queue.poll();
        AudioTrackInfo nextTrackInfo = nextTrack.getTrack().getInfo();
        if(nextTrack != null)
        {
            player.startTrack(nextTrack.getTrack(), false);
            GuildMusicManager player = PlayerManager.getInstance().getMusicManager(nextTrack.getGuild());

            player.setRequester(nextTrack.getRequester());

            EmbedBuilder embed = new EmbedBuilder().setColor(0x409df5);
            embed.setTitle("Now Playing");
            embed.setDescription(String.format("%s `(%s)` [%s]",
                nextTrackInfo.title, ConvertLong.convertLongToTrackTime(nextTrackInfo.length), nextTrack.getRequester().getAsMention()));
            player.getNotifChannel().sendMessageEmbeds(embed.build()).queue();
        }
    }
}
