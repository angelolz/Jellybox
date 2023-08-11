package music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import music.sources.spotify.SpotifyAudioSourceManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager
{
    private static PlayerManager instance;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    private PlayerManager()
    {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        audioPlayerManager.registerSourceManager(new YoutubeAudioSourceManager());
        audioPlayerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        audioPlayerManager.registerSourceManager(new SpotifyAudioSourceManager(new YoutubeAudioSourceManager()));
        audioPlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        audioPlayerManager.registerSourceManager(new BandcampAudioSourceManager());
        audioPlayerManager.registerSourceManager(new HttpAudioSourceManager());
    }

    public GuildMusicManager getMusicManager(Guild guild)
    {
        return musicManagers.computeIfAbsent(guild.getIdLong(),
            guildId -> {
                final GuildMusicManager guildMusicManager = new GuildMusicManager(audioPlayerManager, guild.getAudioManager());

                guild.getAudioManager().setSendingHandler(guildMusicManager.getHandler());

                return guildMusicManager;
            });
    }

    public void loadAndPlay(TextChannel channel, User requester, List<Message.Attachment> attachments)
    {
        final GuildMusicManager guildMusicManager = getMusicManager(channel.getGuild());

        for(Message.Attachment attachment : attachments)
        {
            audioPlayerManager.loadItemOrdered(guildMusicManager, attachment.getUrl(),
                new SourceAudioLoadResultHandler(guildMusicManager, requester, channel));
        }
    }

    public void loadAndPlay(TextChannel channel, User requester, String trackUrl)
    {
        final GuildMusicManager guildMusicManager = getMusicManager(channel.getGuild());

        audioPlayerManager.loadItemOrdered(guildMusicManager, trackUrl,
            new SourceAudioLoadResultHandler(guildMusicManager, requester, channel));
    }

    public static PlayerManager getInstance()
    {
        if(instance == null) instance = new PlayerManager();
        return instance;
    }
}
