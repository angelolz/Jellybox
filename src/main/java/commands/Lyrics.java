package commands;

import com.google.gson.Gson;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import core.GLA;
import main.Jukebox;
import music.GuildMusicManager;
import music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import structure.VideoMetadata;
import utils.LyricsFetcher;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Lyrics extends Command
{

    private GLA lyricsGetter;

    public Lyrics()
    {
        this.name = "lyrics";
        this.help = "Returns lyrics for any song";
        this.cooldown = 3;
        this.arguments = "!lyrics <song-name> - Returns <song-name>'s lyrics\n!lyrics - Returns current song's lyrics";
    }
    @Override
    protected void execute(CommandEvent event)
    {
        try
        {
            AudioTrack track = PlayerManager.getInstance().getMusicManager(event.getGuild()).getScheduler().getPlayer().getPlayingTrack();
            MessageChannel channel = event.getChannel();
            final String search;

            channel.sendTyping().queue();

            if(event.getArgs().isEmpty())
                search = currentSongQuery(track).replaceAll(":", " ");
            else
                search = event.getArgs().replaceAll(":", " ");

            Jukebox.getCache().get(search).whenComplete((formattedLyrics, e) -> {
                if(e == null)
                {
                    // Creates the first page
                    EmbedBuilder embed = new EmbedBuilder().setColor(0x409df5).setTitle("Lyrics Lookup");
                    embed.setDescription(formattedLyrics.get(0));
                    if(formattedLyrics.size() == 1)
                    {
                        event.reply(embed.build());
                    }
                    else
                    {
                        channel.sendMessageEmbeds(embed.build()).setActionRow(
                                Button.secondary("disabled", Emoji.fromUnicode("U+2B05")).asDisabled(),
                                Button.secondary(String.format("%s:pagination:lyrics:right:%s:%s",
                                        event.getMember().getId(), 1, search), Emoji.fromUnicode("U+27A1"))
                        ).queue();
                    }
                }

            });
        }
        catch (IndexOutOfBoundsException e)
        {
            MessageChannel channel = event.getChannel();
            channel.sendMessage("Could not find the song!").queue();
        }
        catch (NullPointerException e)
        {
            MessageChannel channel = event.getChannel();
            channel.sendMessage("There is no song currently playing!\nUse !lyrics <song-name> to search for a specific song.").queue();
            Jukebox.getLogger().error("Null Pointer");
            e.printStackTrace();
        }
        catch (RuntimeException e)
        {
            MessageChannel channel = event.getChannel();
            channel.sendMessage(e.getMessage()).queue();
        }
        catch(IOException e)
        {
            MessageChannel channel = event.getChannel();
            channel.sendMessage("An error has occurred! Please try again later.").queue();
        }
    }

    private String currentSongQuery(AudioTrack track) throws IOException
    {
        String parsedQuery = URLEncoder.encode(track.getInfo().title.toLowerCase().replaceAll("[(\\[].*?[)\\]]",""), StandardCharsets.UTF_8).replaceAll("%23", "#");
        String fullURL = "https://metadata-filter.vercel.app/api/youtube?track=" + parsedQuery;

        URL jsonURL = new URL(fullURL);
        InputStreamReader reader = new InputStreamReader(jsonURL.openStream());

        Gson metadata = new Gson();

        VideoMetadata query = metadata.fromJson(reader, VideoMetadata.class);

        if(!query.getStatus().equals("success"))
        {
            throw new RuntimeException(query.getMessage());
        }

        return query.getData();
    }

    public static void getEmbed(ButtonClickEvent event, int index, String search)
    {
        Jukebox.getCache().get(search).whenComplete((formattedLyrics, e) -> {
            if (e == null)
            {
                EmbedBuilder embed = new EmbedBuilder().setColor(0x409df5).setTitle("Lyrics Lookup");
                embed.setDescription(formattedLyrics.get(index));

                //button checks
                if (index == 0 && formattedLyrics.size() > 1) {
                    event.deferEdit().setEmbeds(embed.build()).setActionRow(
                            Button.secondary("disabled", Emoji.fromUnicode("U+2B05")).asDisabled(),
                            Button.secondary(String.format("%s:pagination:lyrics:right:%s:%s",
                                    event.getMember().getId(), index, search), Emoji.fromUnicode("U+27A1"))
                    ).queue();
                } else if (index == formattedLyrics.size() - 1) {
                    event.deferEdit().setEmbeds(embed.build()).setActionRow(
                            Button.secondary(String.format("%s:pagination:lyrics:left:%s:%s",
                                    event.getMember().getId(), index, search), Emoji.fromUnicode("U+2B05")),
                            Button.secondary("disabled", Emoji.fromUnicode("U+27A1")).asDisabled()
                    ).queue();
                } else {

                    event.deferEdit().setEmbeds(embed.build()).setActionRow(
                            Button.secondary(String.format("%s:pagination:lyrics:left:%s:%s",
                                    event.getMember().getId(), index, search), Emoji.fromUnicode("U+2B05")),
                            Button.secondary(String.format("%s:pagination:lyrics:right:%s:%s",
                                    event.getMember().getId(), index, search), Emoji.fromUnicode("U+27A1"))
                    ).queue();
                }
            }
        });

        Jukebox.getLogger().info(String.valueOf(Jukebox.getCache().synchronous().stats().missCount()));
    }
}
