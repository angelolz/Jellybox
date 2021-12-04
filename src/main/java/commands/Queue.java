package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import main.Jukebox;
import music.GuildMusicManager;
import music.PlayerManager;
import music.TrackScheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.checkerframework.checker.units.qual.C;
import utils.ConvertLong;

import java.awt.*;
import java.util.LinkedList;

public class Queue extends Command
{
    private static final int MAX_ITEMS = 10;

    public Queue()
    {
        this.name = "queue";
        this.aliases = new String[] {"q"};
        this.help = "Returns all the tracks that are in the queue.";
        this.cooldown = 3;

        this.category = new Category("Tools");
    }

    @Override
    protected void execute(CommandEvent event)
    {
        GuildMusicManager manager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        TrackScheduler scheduler = manager.getScheduler();
        LinkedList<AudioTrack> queue = manager.getScheduler().getQueue();
        String[] args = event.getArgs().split("\\s+");

        if(manager.getScheduler().getQueue().isEmpty())
            event.reply(":x: | The queue is empty!");

        else
        {
            if(args[0].equalsIgnoreCase("remove"))
            {
                if(args.length < 2)
                    event.reply(":x: | You need to provide the queue position of the track you want removed!");

                else
                {
                    try
                    {
                        String trackName = scheduler.getQueue().get(Integer.parseInt(args[1]) - 1).getInfo().title;
                        scheduler.getQueue().remove(Integer.parseInt(args[1]) - 1);
                        event.reply(":wastebasket: | Track removed from queue: " + trackName);
                    }

                    catch(IndexOutOfBoundsException e)
                    {
                        event.reply(":x: | That track number does not exist!");
                    }

                    catch(NumberFormatException e)
                    {
                        event.reply(":x: | You've entered an invalid number!");
                    }
                }
            }

            else if(args[0].equalsIgnoreCase("move"))
            {
                if (args.length < 3)
                {
                    event.reply(String.format(":x: | You need to specify what position you want to move the track as well! " +
                            "For example: `%squeue move 5 3` will move the track in the 5th position to the 3rd position.", Jukebox.getPrefix()));
                }

                else
                {
                    try
                    {
                        AudioTrack track = scheduler.getQueue().get(Integer.parseInt(args[1]) - 1); //Save the track to be moved.
                        scheduler.getQueue().remove(Integer.parseInt(args[1]) - 1); //Remove track from original position.
                        scheduler.getQueue().add(Integer.parseInt(args[2]) - 1, track); //Move track to new position.

                        event.reply(String.format(":white_check_mark: | Moved **%s** to position **%s**", track.getInfo().title, args[2]));
                    }

                    catch (IndexOutOfBoundsException e)
                    {
                        event.reply(":x: | That track number or new position does not exist!");
                    }

                    catch (NumberFormatException e)
                    {
                        event.reply(":x: | You've entered an invalid number!");
                    }
                }
            }

            else if(args[0].equalsIgnoreCase("next"))
            {
                if(args.length < 2)
                {
                    event.reply(String.format(":x: | You need to provide the track number that you want played next! " +
                            "For example: `%squeue next 4` will move the track in the 4th position to the top of the queue.", Jukebox.getPrefix()));
                }

                else
                {
                    try
                    {
                        AudioTrack track = scheduler.getQueue().get(Integer.parseInt(args[1]) - 1); //Save the track to be moved.
                        scheduler.getQueue().remove(Integer.parseInt(args[1]) - 1); //Remove track from original position.
                        scheduler.getQueue().addFirst(track); //Move track to front of queue

                        event.reply(String.format(":white_check_mark: | Moved **%s** to the top of the queue!", track.getInfo().title));
                    }

                    catch(IndexOutOfBoundsException e)
                    {
                        event.reply(":x: | That track number!");
                    }

                    catch(NumberFormatException e)
                    {
                        event.reply(":x: | You've entered an invalid number!");
                    }
                }
            }

            else
            {
                EmbedBuilder embed = new EmbedBuilder().setColor(0x409df5);
                embed.setTitle("Current Tracks in Queue");

                int maxPages = queue.size() % MAX_ITEMS == 0 ? queue.size() / MAX_ITEMS : (queue.size() / MAX_ITEMS) + 1;
                embed.setFooter(String.format("Page 1 of %s | Total Tracks in Queue: %s", maxPages, queue.size()));

                AudioPlayer player = manager.getScheduler().getPlayer();
                AudioTrack playingTrack = player.getPlayingTrack();

                int trackNumber = 1;
                for(int i = 0; i < Math.min(MAX_ITEMS, queue.size()); i++)
                {
                    AudioTrack track = queue.get(i);

                    if(!track.getInfo().isStream)
                    {

                        if(trackNumber == 1 && playingTrack != null){//now playing track
                            embed.appendDescription(String.format("**Now Playing)** %s `[%s]` (%s)\n\n", playingTrack.getInfo().title, //labeled as now playing
                            ConvertLong.convertLongToTrackTime(playingTrack.getDuration()), playingTrack.getUserData(User.class).getAsMention()));
                            i--;
                        }
                        else{
                            embed.appendDescription(String.format("**%d)** %s `[%s]` (%s)\n\n", trackNumber, track.getInfo().title,
                                ConvertLong.convertLongToTrackTime(track.getDuration()), track.getUserData(User.class).getAsMention()));
                        }   
                    }

                    else
                    {
                        if(trackNumber == 1 && playingTrack != null){//now playing track
                            embed.appendDescription(String.format("**Now Playing)** %s (%s)\n\n", playingTrack.getInfo().title, //labeled as now playing
                                track.getUserData(User.class).getAsMention()));
                                i--;
                        }
                        else{
                            embed.appendDescription(String.format("**%d)** %s (%s)\n\n", trackNumber, track.getInfo().title,
                                track.getUserData(User.class).getAsMention()));
                        }
                        
                    }
                    
                    trackNumber++;
                }

                if(queue.size() < MAX_ITEMS)
                {
                    event.getChannel().sendMessageEmbeds(embed.build()).setActionRow(
                            Button.secondary("disabled-left", Emoji.fromUnicode("U+2B05")).asDisabled(),
                            Button.secondary(String.format("%s:pagination:queue:refresh:%s:%s",
                                    event.getMember().getId(), 1, event.getGuild().getId()), Emoji.fromUnicode("U+1F504")),
                            Button.secondary("disabled-right", Emoji.fromUnicode("U+27A1")).asDisabled()
                    ).queue();
                }

                else
                {
                    event.getChannel().sendMessageEmbeds(embed.build()).setActionRow(
                            Button.secondary("disabled-left", Emoji.fromUnicode("U+2B05")).asDisabled(),
                            Button.secondary(String.format("%s:pagination:queue:refresh:%s:%s",
                                    event.getMember().getId(), 1, event.getGuild().getId()), Emoji.fromUnicode("U+1F504")),
                            Button.secondary(String.format("%s:pagination:queue:right:%s:%s",
                                    event.getMember().getId(), 1, event.getGuild().getId()), Emoji.fromUnicode("U+27A1"))
                    ).queue();
                }
            }
        }
    }

    public static void getEmbed(ButtonClickEvent event, int pageNum, String guildId)
    {
        LinkedList<AudioTrack> queue = PlayerManager.getInstance()
                .getMusicManager(event.getJDA().getGuildById(guildId)).getScheduler().getQueue();
        int maxPages = queue.size() % MAX_ITEMS == 0 ? queue.size() / MAX_ITEMS : (queue.size() / MAX_ITEMS) + 1;
        int currentPage = pageNum;

        if(currentPage < 1)
            currentPage = 1;

        else if(currentPage > maxPages)
            currentPage = maxPages;

        EmbedBuilder embed = new EmbedBuilder().setColor(0x409df5);
        if(queue.isEmpty())
        {
            embed.setColor(Color.RED);
            embed.setDescription(":x: | The queue is empty!");
            event.deferEdit().setEmbeds(embed.build()).setActionRow(
                    Button.secondary(String.format("%s:pagination:queue:refresh:%s:%s",
                            event.getMember().getId(), currentPage, guildId), Emoji.fromUnicode("U+1F504"))
            ).queue();
        }

        else
        {
            embed.setTitle("Current Tracks in Queue");
            int trackNumber = ((currentPage - 1) * MAX_ITEMS) + 1;

            embed.setFooter(String.format("Page %s of %s | Total Tracks in Queue: %s", currentPage, maxPages, queue.size()));

            for(int i = (currentPage - 1) * MAX_ITEMS; i < Math.min((currentPage * MAX_ITEMS), queue.size()); i++)
            {
                AudioTrack track = queue.get(i);
                embed.appendDescription(String.format("**%d)** %s `[%s]` (%s)\n\n",
                        trackNumber, track.getInfo().title,
                        ConvertLong.convertLongToTrackTime(track.getDuration()),
                        track.getUserData(User.class).getAsMention()));
                trackNumber++;
            }

            //button checks
            if(currentPage == maxPages && maxPages == 1)
            {
                event.deferEdit().setEmbeds(embed.build()).setActionRow(
                        Button.secondary("disabled-left", Emoji.fromUnicode("U+2B05")).asDisabled(),
                        Button.secondary(String.format("%s:pagination:queue:refresh:%s:%s",
                                event.getMember().getId(), 1, event.getGuild().getId()), Emoji.fromUnicode("U+1F504")),
                        Button.secondary("disabled-right", Emoji.fromUnicode("U+27A1")).asDisabled()
                ).queue();
            }

            else if(currentPage == 1 && maxPages > 1)
            {
                event.deferEdit().setEmbeds(embed.build()).setActionRow(
                        Button.secondary("disabled-left", Emoji.fromUnicode("U+2B05")).asDisabled(),
                        Button.secondary(String.format("%s:pagination:queue:refresh:%s:%s",
                                event.getMember().getId(), currentPage, guildId), Emoji.fromUnicode("U+1F504")),
                        Button.secondary(String.format("%s:pagination:queue:right:%s:%s",
                                event.getMember().getId(), currentPage, guildId), Emoji.fromUnicode("U+27A1"))
                ).queue();
            }

            else if(currentPage == maxPages)
            {
                event.deferEdit().setEmbeds(embed.build()).setActionRow(
                        Button.secondary(String.format("%s:pagination:queue:left:%s:%s",
                                event.getMember().getId(), currentPage, guildId), Emoji.fromUnicode("U+2B05")),
                        Button.secondary(String.format("%s:pagination:queue:refresh:%s:%s",
                                event.getMember().getId(), currentPage, guildId), Emoji.fromUnicode("U+1F504")),
                        Button.secondary("disabled-right", Emoji.fromUnicode("U+27A1")).asDisabled()
                ).queue();
            }

            else
            {
                event.deferEdit().setEmbeds(embed.build()).setActionRow(
                        Button.secondary(String.format("%s:pagination:queue:left:%s:%s",
                                event.getMember().getId(), currentPage, guildId), Emoji.fromUnicode("U+2B05")),
                        Button.secondary(String.format("%s:pagination:queue:refresh:%s:%s",
                                event.getMember().getId(), currentPage, guildId), Emoji.fromUnicode("U+1F504")),
                        Button.secondary(String.format("%s:pagination:queue:right:%s:%s",
                                event.getMember().getId(), currentPage, guildId), Emoji.fromUnicode("U+27A1"))
                ).queue();
            }
        }
    }
}