package music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import lombok.Getter;
import music.sources.jellyfin.JellyfinAudioSourceManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {

    private static PlayerManager instance;
    @Getter
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    private PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        audioPlayerManager.registerSourceManager(new JellyfinAudioSourceManager());
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return musicManagers.computeIfAbsent(guild.getIdLong(), guildId -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(audioPlayerManager,
                guild.getAudioManager());

            guild.getAudioManager().setSendingHandler(guildMusicManager.getHandler());

            return guildMusicManager;
        });
    }

    public void loadAndPlay(TextChannel channel, User requester, String trackUrl) {
        final GuildMusicManager guildMusicManager = getMusicManager(channel.getGuild());
        SourceAudioLoadResultHandler sourceAudioLoadResultHandler =
            new SourceAudioLoadResultHandler(guildMusicManager, requester, channel);
        audioPlayerManager.loadItemOrdered(guildMusicManager, trackUrl, sourceAudioLoadResultHandler);
    }

    public static PlayerManager getInstance() {
        if(instance == null)
            instance = new PlayerManager();
        return instance;
    }
}
