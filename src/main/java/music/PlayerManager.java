package music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import main.Jukebox;
import music.sources.spotify.SpotifyAudioSourceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import structure.MusicTrack;
import utils.ConvertLong;

import java.util.HashMap;
import java.util.LinkedList;
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

        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        playerManager.registerSourceManager(new SpotifyAudioSourceManager(new YoutubeAudioSourceManager()));
    }

    public GuildMusicManager getMusicManager(Guild guild)
    {
        return musicManagers.computeIfAbsent(guild.getIdLong(),
            (guildId) -> {
                final GuildMusicManager guildMusicManager = new GuildMusicManager(playerManager, null, guild.getAudioManager());

                guild.getAudioManager().setSendingHandler(guildMusicManager.getHandler());

                return guildMusicManager;
        });
    }

    public void loadAndPlay(TextChannel channel, User requester, String trackUrl)
    {
        final GuildMusicManager guildMusicManager = getMusicManager(channel.getGuild());

        playerManager.loadItemOrdered(guildMusicManager, trackUrl,
            new AudioLoadResultHandler()
            {
                final EmbedBuilder embed = new EmbedBuilder().setColor(0x409df5);

                @Override
                public void trackLoaded(AudioTrack audioTrack)
                {
                    AudioTrackInfo trackInfo = audioTrack.getInfo();
                    LinkedList<MusicTrack> queue = guildMusicManager.getScheduler().getQueue();
                    AudioPlayer player = guildMusicManager.getScheduler().getPlayer();


                    if(guildMusicManager.getScheduler().queue(audioTrack, requester))
                    {
                        embed.setTitle("Added to Queue!");
                        embed.addField("Artist", trackInfo.author, true);
                        embed.addField("Title", trackInfo.title, true);
                        embed.addField("Length",  ConvertLong.convertLongToTrackTime(trackInfo.length), true);

                        embed.addField("Requested by:", requester.getAsMention(), true);
                        embed.addField("Position in queue", String.valueOf(queue.size()), true);

                        //calculate time left before song plays
                        long totalQueueLength = 0;
                        totalQueueLength += player.getPlayingTrack().getPosition();
                        for(MusicTrack track : queue)
                        {
                            totalQueueLength += track.getTrack().getDuration();
                        }

                        embed.addField("Time before song plays", ConvertLong.convertLongToTrackTime(totalQueueLength),true);
                    }

                    else
                    {
                        PlayerManager.getInstance().getMusicManager(channel.getGuild()).setRequester(requester);

                        embed.setTitle("Now Playing");
                        embed.setDescription(String.format("%s `[%s]` (%s)",
                            trackInfo.title, ConvertLong.convertLongToTrackTime(trackInfo.length), requester.getAsMention()));
                    }

                    channel.sendMessageEmbeds(embed.build()).queue();
                }

                @Override
                public void playlistLoaded(AudioPlaylist audioPlaylist)
                {
                    if(audioPlaylist.isSearchResult())
                       trackLoaded(audioPlaylist.getTracks().get(0));

                    else
                    {
                        long playlistDuration = 0;
                        for(AudioTrack track: audioPlaylist.getTracks())
                        {
                            guildMusicManager.getScheduler().queue(track, requester);
                            playlistDuration += track.getDuration();
                        }

                        embed.setDescription(String.format(":notes: Added **%d** songs to the queue!", audioPlaylist.getTracks().size()));
                        embed.addField("Total time added:", ConvertLong.convertLongToTrackTime(playlistDuration), true);
                        embed.addField("Requested by:", requester.getAsMention(), true);
                        channel.sendMessageEmbeds(embed.build()).queue();
                    }
                }

                @Override
                public void noMatches()
                {
                    channel.sendMessage(":x: | Sorry, we couldn't find a matching result for your request!").queue();
                }

                @Override
                public void loadFailed(FriendlyException e)
                {
                    channel.sendMessage(":x: | There was an error trying to play your song.").queue();
                    Jukebox.getLogger().error("Error occurred when playing song: {}: {}", e.getClass().getName(), e.getMessage());
                }
            });
    }

    public static PlayerManager getInstance()
    {
        if(instance == null) instance = new PlayerManager();
        return instance;
    }
}
