package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import music.GuildMusicManager;
import music.PlayerManager;
import music.TrackScheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import structure.MusicTrack;
import utils.ConvertLong;

import java.util.LinkedList;

public class Queue extends Command
{
    public Queue()
    {
        this.name = "queue";
        this.help = "`!queue`, `!queue`: Returns list of song in the queue";
        this.aliases = new String[] {"q"};
        this.cooldown = 3;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        GuildMusicManager manager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        TrackScheduler scheduler = manager.getScheduler();
        LinkedList<MusicTrack> queue = manager.getScheduler().getQueue(); //Linked list containing the current queue

        String[] args = event.getArgs().split("\\s+"); //Split arguments into array of strings

        if(manager.getScheduler().getQueue().isEmpty()) //If queue is empty
        {
            event.reply(":x: | The queue is empty!");
        }

        else if(args[0].equalsIgnoreCase("list") || event.getArgs().isEmpty()) //If user uses "list" sub-command or no sub-command is specified
        {
            EmbedBuilder embed = new EmbedBuilder().setColor(0x409df5);
            embed.setTitle("Current Songs in Queue");
            int trackNumber = 1;

            for(MusicTrack mTrack : queue)
            {
                embed.appendDescription(String.format("**%d)** %s `[%s]` (%s)\n\n",
                        trackNumber, mTrack.getTrack().getInfo().title,
                        ConvertLong.convertLongToTrackTime(mTrack.getTrack().getDuration()),
                        mTrack.getRequester().getAsMention()));
                trackNumber++;
            }

            event.reply(embed.build());
        }

        else if(args[0].equalsIgnoreCase("remove")) //If user uses "remove" sub-command.
        {
            if(args.length == 2)
            {
                try
                {
                    String trackName = scheduler.getQueue().get(Integer.parseInt(args[1]) - 1).getTrack().getInfo().title;
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
                    MusicTrack track = scheduler.getQueue().get(Integer.parseInt(args[1]) - 1); //Save the track to be moved.
                    scheduler.getQueue().remove(Integer.parseInt(args[1]) - 1); //Remove track from original position.
                    scheduler.getQueue().add(Integer.parseInt(args[2]) - 1, track); //Move track to new position.

                    event.reply(String.format(":white_check_mark: | Moved **%s** to position **%s**", track.getTrack().getInfo().title, args[2]));
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

        else
        {
            event.reply(":x: | Invalid argument(s), type `!help` to see list of commands!");
        }
    }
}