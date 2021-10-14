package music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class GuildMusicManager
{
    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private final AudioPlayerSendHandler handler;

    public GuildMusicManager(AudioPlayerManager manager)
    {
        this.player = manager.createPlayer();
        this.scheduler = new TrackScheduler(player);
        this.handler = new AudioPlayerSendHandler(player);

        player.addListener(scheduler);
    }

    public AudioPlayerSendHandler getHandler()
    {
        return handler;
    }

    public TrackScheduler getScheduler()
    {
        return scheduler;
    }
}
