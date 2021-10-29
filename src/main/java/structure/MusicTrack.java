package structure;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;


/**
 * 
 * @author Andrew Carlson, Angel Legaspi
 */
public class MusicTrack
{
    private AudioTrack track;
    private User requester;

    public MusicTrack(AudioTrack track, User requester)
    {
        this.track = track;
        this.requester = requester;
    }

    public AudioTrack getTrack() { return track; }
    public User getRequester() { return requester; }
}
