package music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import main.Jukebox;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import utils.ConvertLong;
import utils.URLUtils;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager
{
    private static PlayerManager instance;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager playerManager;

    private PlayerManager()
    {
        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public GuildMusicManager getMusicManager(Guild guild)
    {
        return musicManagers.computeIfAbsent(guild.getIdLong(),
            (guildId) -> {
                final GuildMusicManager guildMusicManager = new GuildMusicManager(playerManager);

                guild.getAudioManager().setSendingHandler(guildMusicManager.getHandler());

                return guildMusicManager;
        });
    }

    public void loadAndPlay(TextChannel channel, String trackUrl)
    {
        final GuildMusicManager guildMusicManager = getMusicManager(channel.getGuild());

        playerManager.loadItemOrdered(guildMusicManager, trackUrl,
            new AudioLoadResultHandler()
            {
                @Override
                public void trackLoaded(AudioTrack audioTrack)
                {
                    if(guildMusicManager.getScheduler().queue(audioTrack))
                    {
                        channel.sendMessageFormat(":notes: Added to queue: `%s - %s` **[%s]**",
                            audioTrack.getInfo().author, audioTrack.getInfo().title,
                            ConvertLong.convertLongToTrackTime(audioTrack.getInfo().length)).queue();
                    }

                    else
                    {
                        channel.sendMessageFormat(":musical_note: Now Playing: `%s - %s` **[%s]**",
                            audioTrack.getInfo().author, audioTrack.getInfo().title,
                            ConvertLong.convertLongToTrackTime(audioTrack.getInfo().length)).queue();
                    }
                }

                @Override
                public void playlistLoaded(AudioPlaylist audioPlaylist)
                {
                    if(audioPlaylist.isSearchResult())
                    {
                       trackLoaded(audioPlaylist.getTracks().get(0));
                    }

                    else
                    {
                        for(AudioTrack track: audioPlaylist.getTracks())
                        {
                            guildMusicManager.getScheduler().queue(track);
                        }

                        channel.sendMessageFormat(":notes: Added **%d** songs to the queue!", audioPlaylist.getTracks().size()).queue();
                    }
                }

                @Override
                public void noMatches()
                {

                }

                @Override
                public void loadFailed(FriendlyException e)
                {
                    channel.sendMessage(":x: | There was an error trying to play your song.").queue();
                    Jukebox.getLogger().error("Error occurred when playing song: {}: ", e.getClass().getName(), e.getMessage());
                }
            });
    }

    public static PlayerManager getInstance()
    {
        if(instance == null) instance = new PlayerManager();
        return instance;
    }
}
