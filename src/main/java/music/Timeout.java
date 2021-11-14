package music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.Timer;
import java.util.TimerTask;

public class Timeout extends AudioEventAdapter
{
    private AudioManager audioManager;
    private TrackScheduler scheduler;
    private Timer countdown;
    private final long DELAY;

    public Timeout(AudioManager audioManager, TrackScheduler scheduler)
    {
        DELAY = 600000; // Sets the delay to 10 minutes

        this.audioManager = audioManager;
        this.scheduler = scheduler;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason)
    {
        startTimer();
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track)
    {
        if(countdown != null)
        {
            cancelTimer();
        }
    }

    @Override
    public void onPlayerPause(AudioPlayer player)
    {
        startTimer();
    }

    @Override
    public void onPlayerResume(AudioPlayer player)
    {
        cancelTimer();
    }

    public void startTimer()
    {
        countdown = new Timer();
        countdown.schedule(new DisconnectProcess(), DELAY); // Start an idle timer
    }

    public void cancelTimer()
    {
        countdown.cancel();
    }

    /**
     * Disconnects the bot from the voice channel.
     */
    class DisconnectProcess extends TimerTask
    {
        @Override
        public void run()
        {
            // Checks if the player is not playing anything
            if((scheduler.getPlayer().getPlayingTrack() == null || scheduler.getPlayer().isPaused())&& audioManager.isConnected())
            {
                // Disconnect if its not playing anything
                audioManager.closeAudioConnection(); // Disconnect from the channel
                scheduler.getPlayer().stopTrack(); // Stops the track
                scheduler.getPlayer().setPaused(false); // Unpauses the player
                scheduler.setLoopState(LoopState.DISABLED);
                scheduler.getQueue().clear(); // Clears the queue

                scheduler.getNotifChannel().sendMessage(":zzz: | Leaving due to inactivity.").queue();
            }
        }
    }
}
