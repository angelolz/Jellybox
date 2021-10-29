package music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import main.Jukebox;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import structure.MusicTrack;
import utils.ConvertLong;

import java.awt.*;
import java.util.LinkedList;

public class TrackScheduler extends AudioEventAdapter
{
    private final AudioPlayer player;
    private final LinkedList<MusicTrack> queue;
    private TextChannel notifChannel;

    public TrackScheduler(AudioPlayer player)
    {
         this.player = player;
         this.queue = new LinkedList<>();
    }

    public boolean queue(AudioTrack track, User requester)
    {
        if(!this.player.startTrack(track, true))
        {
            MusicTrack requestTrack = new MusicTrack(track, requester);
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

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException e)
    {
        EmbedBuilder embed = new EmbedBuilder().setColor(Color.red);
        embed.setTitle("Error!");
        embed.setDescription(":x: Couldn't play track!");
        embed.addField("Song", track.getInfo().title, false);

        //TODO use this code when MusicTrack gets removed
//        embed.addField("Requested By", track.getUserData(User.class).getAsMention(), false);

        notifChannel.sendMessageEmbeds(embed.build()).queue();
        Jukebox.getLogger().error("Error occurred when playing song: {}: {}", e.getClass().getName(), e.getMessage());
    }

    public AudioPlayer getPlayer()
    {
        return player;
    }

    public LinkedList<MusicTrack> getQueue()
    {
        return queue;
    }

    public void setNotifChannel(TextChannel channel)
    {
        this.notifChannel = channel;
    }

    public TextChannel getNotifChannel()
    {
        return notifChannel;
    }

    private void nextTrack()
    {
        if(queue.peek() != null)
        {
            MusicTrack nextTrack = queue.poll();
            AudioTrackInfo nextTrackInfo = nextTrack.getTrack().getInfo();

            GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(notifChannel.getGuild());
            musicManager.setRequester(nextTrack.getRequester());

            player.startTrack(nextTrack.getTrack(), false);

            EmbedBuilder embed = new EmbedBuilder().setColor(0x409df5);
            embed.setTitle("Now Playing");
            embed.setDescription(String.format("%s `(%s)` [%s]",
                nextTrackInfo.title, ConvertLong.convertLongToTrackTime(nextTrackInfo.length), nextTrack.getRequester().getAsMention()));
            notifChannel.sendMessageEmbeds(embed.build()).queue();
        }
    }
}
