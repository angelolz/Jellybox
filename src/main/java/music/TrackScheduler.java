package music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter
{
    public final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;

    public TrackScheduler(AudioPlayer player)
    {
         this.player = player;
         this.queue = new LinkedBlockingQueue<>();
    }

    public boolean queue(AudioTrack track)
    {
        if(!this.player.startTrack(track, true))
        {
            queue.offer(track);
            return true;
        }

        else
            return false;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason)
    {
        if(endReason.mayStartNext)
        {
            nextTrack();
        }
    }

    public AudioPlayer getPlayer()
    {
        return player;
    }

    public BlockingQueue<AudioTrack> getQueue()
    {
        return queue;
    }

    private void nextTrack()
    {
        player.startTrack(queue.poll(), false);
    }
}
