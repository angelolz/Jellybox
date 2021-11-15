package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import music.GuildMusicManager;
import music.PlayerManager;
import music.TrackScheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;
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
    }

    @Override
    protected void execute(CommandEvent event)
    {
        GuildMusicManager manager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        TrackScheduler scheduler = manager.getScheduler();
        LinkedList<AudioTrack> queue = manager.getScheduler().getQueue(); //Linked list containing the current queue

        String[] args = event.getArgs().split("\\s+"); //Split arguments into array of strings

        if(manager.getScheduler().getQueue().isEmpty()) //If queue is empty
        {
            event.reply(":x: | The queue is empty!");
        }

        else if(args[0].equalsIgnoreCase("list") || event.getArgs().isEmpty()) //If user uses "list" sub-command or no sub-command is specified
        {
            EmbedBuilder embed = new EmbedBuilder().setColor(0x409df5);
            embed.setTitle("Current Songs in Queue");

            int maxPages = queue.size() % MAX_ITEMS == 0 ? queue.size() / MAX_ITEMS : (queue.size() / MAX_ITEMS) + 1;
            embed.setFooter(String.format("Page 1 of %s | Total Songs in Queue: %s", maxPages, queue.size()));

            int trackNumber = 1;
            for(int i = 0; i < Math.min(MAX_ITEMS, queue.size()); i++)
            {
                AudioTrack track = queue.get(i);

                embed.appendDescription(String.format("**%d)** %s `[%s]` (%s)\n\n",
                        trackNumber, track.getInfo().title,
                        ConvertLong.convertLongToTrackTime(track.getDuration()),
                        track.getUserData(User.class).getAsMention()));
                trackNumber++;
            }

            event.getChannel().sendMessageEmbeds(embed.build()).setActionRow(
                    Button.secondary("disabled", Emoji.fromUnicode("U+2B05")).asDisabled(),
                    Button.secondary(String.format("%s:pagination:queue:refresh:%s:%s",
                            event.getMember().getId(), 1, event.getGuild().getId()), Emoji.fromUnicode("U+1F504")),
                    Button.secondary(String.format("%s:pagination:queue:right:%s:%s",
                            event.getMember().getId(), 1, event.getGuild().getId()), Emoji.fromUnicode("U+27A1"))
            ).queue();
        }

        else if(args[0].equalsIgnoreCase("remove")) //If user uses "remove" sub-command.
        {
            if(args.length == 2)
            {
                try
                {
                    String trackName = scheduler.getQueue().get(Integer.parseInt(args[1]) - 1).getInfo().title;
                    scheduler.getQueue().remove(Integer.parseInt(args[1]) - 1);
                    event.reply(":wastebasket: | Song removed from queue: " + trackName);
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

            else //If no track number is given or user does not use correct format.
            {
                event.reply(":x: | You need to provide a number!");
            }
        }

        else if(args[0].equalsIgnoreCase("move"))
        {
            if(args.length == 2)
            {
                event.reply(":x: | You need to specify what position you want to move the track as well!");
            }

            if(args.length == 3)
            {
                try
                {
                    AudioTrack track = scheduler.getQueue().get(Integer.parseInt(args[1]) - 1); //Save the track to be moved.
                    scheduler.getQueue().remove(Integer.parseInt(args[1]) - 1); //Remove track from original position.
                    scheduler.getQueue().add(Integer.parseInt(args[2]) - 1, track); //Move track to new position.

                    event.reply(String.format(":white_check_mark: | Moved **%s** to position **%s**", track.getInfo().title, args[2]));
                }

                catch(IndexOutOfBoundsException e)
                {
                    event.reply(":x: | That track number or new position does not exist!");
                }

                catch(NumberFormatException e)
                {
                    event.reply(":x: | You've entered an invalid number!");
                }
            }

            else //If no index is given or user does not use correct format
            {
                event.reply(":x: | You need to provide a track number and what position you want to move it to!");
            }
        }
        else if(args[0].equalsIgnoreCase("next"))
        {
            if(args.length == 2)
            {
                try
                {
                    AudioTrack track = scheduler.getQueue().get(Integer.parseInt(args[1]) - 1); //Save the track to be moved.
                    scheduler.getQueue().remove(Integer.parseInt(args[1]) - 1); //Remove track from original position.
                    scheduler.getQueue().add(0, track); //Move track to front of queue

                    event.reply(String.format(":white_check_mark: | Moved **%s** to next song", track.getInfo().title));
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
            else
            {
                event.reply(":x: | You need to provide the track number that you want played next!");
            }      
        }
        else
        {
            event.reply(":x: | Invalid argument(s), type `!help` to see list of commands!");
        }
    }

    public static void getEmbed(ButtonClickEvent event, int pageNum, String guildId)
    {
        LinkedList<AudioTrack> queue = PlayerManager.getInstance()
                .getMusicManager(event.getJDA().getGuildById(guildId)).getScheduler().getQueue();
        int maxPages = queue.size() % MAX_ITEMS == 0 ? queue.size() / MAX_ITEMS : (queue.size() / MAX_ITEMS) + 1;
        int currentPage = pageNum;

        if(currentPage < 1)
        {
            currentPage = 1;
        }

        else if(currentPage > maxPages)
        {
            currentPage = maxPages;
        }

        EmbedBuilder embed = new EmbedBuilder().setColor(0x409df5);
        if(queue.isEmpty())
        {
            embed.setColor(Color.RED);
            embed.setDescription(":x: The queue is empty!");
            event.deferEdit().setEmbeds(embed.build()).setActionRow(
                    Button.secondary(String.format("%s:pagination:queue:refresh:%s:%s",
                            event.getMember().getId(), currentPage, guildId), Emoji.fromUnicode("U+1F504"))
            ).queue();
        }

        else
        {
            embed.setTitle("Current Songs in Queue");
            int trackNumber = ((currentPage - 1) * MAX_ITEMS) + 1;

            embed.setFooter(String.format("Page %s of %s | Total Songs in Queue: %s", currentPage, maxPages, queue.size()));

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
                        Button.secondary("disabled", Emoji.fromUnicode("U+2B05")).asDisabled(),
                        Button.secondary(String.format("%s:pagination:queue:refresh:%s:%s",
                                event.getMember().getId(), 1, event.getGuild().getId()), Emoji.fromUnicode("U+1F504")),
                        Button.secondary("disabled", Emoji.fromUnicode("U+27A1")).asDisabled()
                ).queue();
            }

            else if(currentPage == 1 && maxPages > 1)
            {
                event.deferEdit().setEmbeds(embed.build()).setActionRow(
                        Button.secondary("disabled", Emoji.fromUnicode("U+2B05")).asDisabled(),
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
                        Button.secondary("disabled", Emoji.fromUnicode("U+27A1")).asDisabled()
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