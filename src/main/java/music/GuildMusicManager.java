package music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.w3c.dom.Text;

public class GuildMusicManager
{
    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private final AudioPlayerSendHandler handler;
    private User currentSongRequester;
    private TextChannel notifChannel;

    public GuildMusicManager(AudioPlayerManager manager, User requester)
    {
        this.player = manager.createPlayer();
        this.scheduler = new TrackScheduler(player);
        this.handler = new AudioPlayerSendHandler(player);
        this.currentSongRequester = requester;

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

    public User getRequester()
    {
        return currentSongRequester;
    }

    public void setRequester(User requester)
    {
        this.currentSongRequester = requester;
    }

    public TextChannel getNotifChannel() { return notifChannel; }

    public void setNotifChannel(TextChannel channel)
    {
        if(notifChannel == null)
            this.notifChannel = channel;
    }
}
