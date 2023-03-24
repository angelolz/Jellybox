package music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import main.Jukebox;
import music.sources.spotify.SpotifyAudioSourceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import utils.ConvertLong;
import utils.ThumbnailGrabber;

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
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
    }

    public GuildMusicManager getMusicManager(Guild guild)
    {
        return musicManagers.computeIfAbsent(guild.getIdLong(),
                (guildId) -> {
                    final GuildMusicManager guildMusicManager = new GuildMusicManager(playerManager, guild.getAudioManager());

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
                        LinkedList<AudioTrack> queue = guildMusicManager.getScheduler().getQueue();
                        AudioPlayer player = guildMusicManager.getScheduler().getPlayer();

                        if(guildMusicManager.getScheduler().queue(audioTrack, requester))
                        {
                            embed.setTitle("Added to Queue!");
                            embed.setThumbnail(ThumbnailGrabber.getThumbnail(audioTrack));
                            embed.addField("Artist", trackInfo.author, true);
                            embed.addField("Title", trackInfo.title, true);
                            if(!trackInfo.isStream)
                            {
                                embed.addField("Length",  ConvertLong.convertLongToTrackTime(trackInfo.length), true);
                            }

                            embed.addField("Requested by:", requester.getAsMention(), true);
                            embed.addField("Position in queue", String.valueOf(queue.size()), true);

                            //calculate time left before track plays
                            long totalQueueLength = 0;
                            boolean hasStream = false;

                            if(!player.getPlayingTrack().getInfo().isStream)
                                totalQueueLength += player.getPlayingTrack().getDuration() - player.getPlayingTrack().getPosition();
                            else
                                hasStream = true;

                            for(AudioTrack track : queue)
                            {
                                if(!track.getInfo().isStream)
                                    totalQueueLength += track.getDuration();
                                else
                                    hasStream = true;
                            }

                            if(hasStream)
                            {
                                embed.addField("Time before track plays*", ConvertLong.convertLongToTrackTime(totalQueueLength),true);
                                embed.setFooter("* This does not include the livestreams that are currently playing and/or added in the queue.");
                            }

                            else
                            {
                                embed.addField("Time before track plays", ConvertLong.convertLongToTrackTime(totalQueueLength),true);
                            }
                        }

                        else
                        {
                            embed.setTitle("Now Playing");
                            embed.setThumbnail(ThumbnailGrabber.getThumbnail(audioTrack));
                            if(trackInfo.isStream)
                                embed.setDescription(String.format("%s (%s)", trackInfo.title, requester.getAsMention()));
                            else
                                embed.setDescription(String.format("%s `[%s]` (%s)", trackInfo.title, ConvertLong.convertLongToTrackTime(trackInfo.length), requester.getAsMention()));
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
                            int tracksAdded = 0;
                            for(AudioTrack track: audioPlaylist.getTracks())
                            {
                                if(tracksAdded < 250)
                                {
                                    guildMusicManager.getScheduler().queue(track, requester);
                                    playlistDuration += track.getDuration();
                                    tracksAdded++;
                                }

                                else
                                    break;
                            }

                            embed.setDescription(String.format(":notes: Added **%d** tracks to the queue!", tracksAdded));
                            embed.addField("Total time added:", ConvertLong.convertLongToTrackTime(playlistDuration), true);
                            embed.addField("Requested by:", requester.getAsMention(), true);

                            if(tracksAdded <= 250 && audioPlaylist.getTracks().size() > 250)
                            {
                                embed.appendDescription("\n\nThe playlist you've added is too large, so I've added only the first **250** tracks " +
                                        "to the queue.");
                            }

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
                        channel.sendMessage(":x: | There was an error trying to play your track.").queue();
                        Jukebox.getLogger().error("Error occurred when playing track: {}: {}", e.getClass().getName(), e.getMessage());
                    }
                });
    }

    public static PlayerManager getInstance()
    {
        if(instance == null) instance = new PlayerManager();
        return instance;
    }
}
