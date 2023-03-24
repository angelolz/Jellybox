package music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public class GuildMusicManager
{
    private final TrackScheduler scheduler;
    private final AudioPlayerSendHandler handler;
    private final Timeout timeout;
    private TextChannel notifChannel;

    public GuildMusicManager(AudioPlayerManager manager, AudioManager audioManager)
    {
        AudioPlayer player = manager.createPlayer();
        this.scheduler = new TrackScheduler(player);
        this.handler = new AudioPlayerSendHandler(player);
        this.timeout = new Timeout(audioManager, this.scheduler); // Add timeout object for timeout
        player.addListener(scheduler);
        player.addListener(timeout); // Adds listener to timer
    }

    public AudioPlayerSendHandler getHandler()
    {
        return handler;
    }

    public TrackScheduler getScheduler()
    {
        return scheduler;
    }

    public Timeout getTimer(){
        return timeout;
    }

    public void setNotifChannel(TextChannel channel)
    {
        if(notifChannel == null)
        {
            this.notifChannel = channel;
            scheduler.setNotifChannel(notifChannel);
        }
    }

    public void resetNotifChannel()
    {
        this.notifChannel = null;
        scheduler.setNotifChannel(null);
    }
}
