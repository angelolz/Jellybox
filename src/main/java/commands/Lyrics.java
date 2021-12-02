package commands;

import com.google.gson.Gson;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import main.Jukebox;
import music.PlayerManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.checkerframework.checker.units.qual.C;
import structure.VideoMetadata;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Lyrics extends Command
{
    public Lyrics()
    {
        this.name = "lyrics";
        this.arguments = "[query]";
        this.help = "Returns lyrics for a track.";
        this.cooldown = 3;

        this.category = new Category("Tools");
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
            {
                if(track != null)
                {
                    if(track.getInfo().isStream)
                    {
                        event.reply(":x: | Searching for lyrics aren't supported for live streams. Please add a query to search for lyrics!");
                        return;
                    }

                    else
                        search = currentTrackQuery(track).replaceAll(":", " ");
                }

                else
                {
                    event.reply(":x: | There is no track currently playing!\nUse !lyrics <track-name> to search for a specific track.");
                    return;
                }
            }

            else
                search = event.getArgs().replaceAll(":", " ");

            Jukebox.getCache().get(search).whenComplete((formattedLyrics, e) -> {
                if(e == null)
                {
                    // Creates the first page
                    EmbedBuilder embed = new EmbedBuilder()
                            .setColor(0x409df5)
                            .setTitle("Lyrics Lookup")
                            .setDescription("Requested by: " + event.getAuthor().getAsTag());

                    embed.setDescription(formattedLyrics.get(0));

                    if(formattedLyrics.size() == 1)
                        event.reply(embed.build());

                    else
                    {
                        channel.sendMessageEmbeds(embed.build()).setActionRow(
                                Button.secondary("disabled-left", Emoji.fromUnicode("U+2B05")).asDisabled(),
                                Button.secondary(String.format("%s:pagination:lyrics:right:%s:%s",
                                        event.getMember().getId(), 0, search), Emoji.fromUnicode("U+27A1"))
                        ).queue();
                    }
                }

                else
                {
                    switch(e.getCause().getClass().getSimpleName())
                    {
                        case "IndexOutOfBoundsException":
                            event.reply(":x: | Couldn't find the lyrics for the requested track!");
                            break;
                        case "RuntimeException":
                            event.reply(":x: | " + e.getMessage());
                            break;
                        default:
                            event.reply(":x: | An error has occurred! Please try again later.");
                            Jukebox.getLogger().error("{} Error: {}", e.getCause().getClass().getSimpleName(), e.getMessage());
                            break;
                    }
                }
            });
        }

        catch (NullPointerException e)
        {
            event.reply(":x: | There is no track currently playing! Use !lyrics <track-name> to search for a specific track.");
        }

        catch(Exception e)
        {
            event.reply(":x: | An error has occurred! Please try again later.");
        }
    }

    private String currentTrackQuery(AudioTrack track) throws IOException
    {
        String parsedQuery = URLEncoder.encode(track.getInfo().title.toLowerCase().replaceAll("[(\\[].*?[)\\]]",""), StandardCharsets.UTF_8).replaceAll("%23", "#");
        String fullURL = "https://metadata-filter.vercel.app/api/youtube?track=" + parsedQuery;

        URL jsonURL = new URL(fullURL);
        InputStreamReader reader = new InputStreamReader(jsonURL.openStream());

        Gson metadata = new Gson();

        VideoMetadata query = metadata.fromJson(reader, VideoMetadata.class);

        if(!query.getStatus().equals("success"))
            throw new RuntimeException(query.getMessage());

        return query.getData();
    }

    public static void getEmbed(ButtonClickEvent event, int index, String search)
    {
        Jukebox.getCache().get(search).whenComplete((formattedLyrics, e) -> {
            if (e == null)
            {
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(0x409df5)
                        .setTitle("Lyrics Lookup")
                        .setFooter("Requested by: " + event.getUser().getAsTag());
                embed.setDescription(formattedLyrics.get(index));

                //button checks
                if (index == 0 && formattedLyrics.size() > 1)
                {
                    event.deferEdit().setEmbeds(embed.build()).setActionRow(
                            Button.secondary("disabled-left", Emoji.fromUnicode("U+2B05")).asDisabled(),
                            Button.secondary(String.format("%s:pagination:lyrics:right:%s:%s",
                                    event.getMember().getId(), index, search), Emoji.fromUnicode("U+27A1"))
                    ).queue();
                }

                else if (index == formattedLyrics.size() - 1)
                {
                    event.deferEdit().setEmbeds(embed.build()).setActionRow(
                            Button.secondary(String.format("%s:pagination:lyrics:left:%s:%s",
                                    event.getMember().getId(), index, search), Emoji.fromUnicode("U+2B05")),
                            Button.secondary("disabled-right", Emoji.fromUnicode("U+27A1")).asDisabled()
                    ).queue();
                }

                else
                {

                    event.deferEdit().setEmbeds(embed.build()).setActionRow(
                            Button.secondary(String.format("%s:pagination:lyrics:left:%s:%s",
                                    event.getMember().getId(), index, search), Emoji.fromUnicode("U+2B05")),
                            Button.secondary(String.format("%s:pagination:lyrics:right:%s:%s",
                                    event.getMember().getId(), index, search), Emoji.fromUnicode("U+27A1"))
                    ).queue();
                }
            }
        });
    }
}
