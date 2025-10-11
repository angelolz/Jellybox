package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import main.Config;
import music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import utils.Statics;
import utils.UtilClass;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Queue extends Command
{
    public Queue()
    {
        this.name = "queue";
        this.aliases = new String[]{ "q" };
        this.help = "Returns all the tracks that are in the queue.";
        this.cooldown = 3;

        this.category = new Category("Tools");
    }

    @Override
    protected void execute(CommandEvent event)
    {
        LinkedList<AudioTrack> queue = PlayerManager.getInstance().getMusicManager(event.getGuild()).getScheduler().getQueue();
        AudioTrack playingTrack = PlayerManager.getInstance().getMusicManager(event.getGuild()).getScheduler().getPlayer().getPlayingTrack();
        String[] args = event.getArgs().split("\\s+");

        if(queue.isEmpty())
        {
            event.replyError("The queue is empty!");
            return;
        }

        switch(args[0].toLowerCase())
        {
            case "remove" -> removeTrack(event, args, queue);
            case "move" -> moveTrack(event, queue, args);
            default ->
            {
                EmbedBuilder embed = printEmbed(queue, playingTrack, 1);
                int maxPages = getMaxPages(queue);
                event.getChannel().sendMessageEmbeds(embed.build()).setComponents(ActionRow.of(getPaginationButtons(event.getAuthor().getId(), event.getGuild().getId(), 1, maxPages))).queue();
            }
        }
    }

    private static void removeTrack(CommandEvent event, String[] args, LinkedList<AudioTrack> queue)
    {
        if(args.length < 2)
        {
            event.replyError("You need to provide the queue position of the track you want removed!");
            return;
        }

        Integer trackNum = checkNum(event, queue, args[1]);
        if(trackNum == null) return;

        AudioTrack track = queue.remove(trackNum.intValue());
        event.replyFormatted(":wastebasket: | Track removed from queue: **%s**", track.getInfo().title);
    }

    private static void moveTrack(CommandEvent event, LinkedList<AudioTrack> queue, String[] args)
    {
        if(args.length < 3)
        {
            event.replyFormatted("❌ | You need to specify what position you want to move the track as well! " +
                "For example: `%squeue move 5 3` will move the track in the 5th position to the 3rd position.", Config.getPrefix());
            return;
        }

        Integer oldPos = checkNum(event, queue, args[1]);
        if(oldPos == null) return;

        Integer newPos = checkNum(event, queue, args[2]);
        if(newPos == null) return;

        AudioTrack track = queue.remove(oldPos.intValue()); //Remove track from original position.
        queue.add(newPos, track); //Move track to new position.

        event.replyFormatted("✅ | Moved **%s** to position **%s**", track.getInfo().title, args[2]);
    }

    private static Integer checkNum(CommandEvent event, LinkedList<AudioTrack> queue, String num)
    {
        if(isNumber(num))
        {
            event.replyError("You've entered an invalid number!");
            return null;
        }

        int trackNum = Integer.parseInt(num) - 1;
        if(!isWithinRange(queue, trackNum))
        {
            event.replyError("That track number does not exist!");
            return null;
        }

        return trackNum;
    }

    private static boolean isNumber(String num)
    {
        try
        {
            Integer.parseInt(num);
            return true;
        }

        catch(NumberFormatException e)
        {
            return false;
        }
    }

    private static boolean isWithinRange(LinkedList<AudioTrack> queue, int num)
    {
        return num > 0 && num < queue.size();
    }

    private static int getMaxPages(LinkedList<AudioTrack> queue)
    {
        return queue.size() % Statics.MAX_ITEMS_PER_PAGE == 0 ? queue.size() / Statics.MAX_ITEMS_PER_PAGE : (queue.size() / Statics.MAX_ITEMS_PER_PAGE) + 1;
    }

    private static EmbedBuilder printEmbed(LinkedList<AudioTrack> queue, AudioTrack playingTrack, int pageNum)
    {
        EmbedBuilder embed = new EmbedBuilder().setColor(Statics.EMBED_COLOR);
        int maxPages = getMaxPages(queue);

        embed.setTitle("Current Tracks in Queue")
             .setFooter(String.format("Page %s of %s | Total Tracks in Queue: %s", pageNum, maxPages, queue.size()));

        if(playingTrack != null)
        {
            embed.addField("Now Playing",
                String.format("%s `[%s]` (%s)", playingTrack.getInfo().title, UtilClass.convertLongToTrackTime(playingTrack.getDuration()),
                    playingTrack.getUserData(User.class).getAsMention()),
                false);
        }

        if(queue.isEmpty())
        {
            embed.setColor(Color.RED)
                 .addField("Queue List", "❌ | The queue is empty!", false)
                 .setFooter("");

            return embed;
        }

        StringBuilder queueString = new StringBuilder();
        int trackNumber = ((pageNum - 1) * Statics.MAX_ITEMS_PER_PAGE) + 1;
        for(int i = (pageNum - 1) * Statics.MAX_ITEMS_PER_PAGE; i < Math.min((pageNum * Statics.MAX_ITEMS_PER_PAGE), queue.size()); i++)
        {
            AudioTrack track = queue.get(i);
            String trackFormat = "**%d)** %s - %s %s (%s)%n";
            String duration = UtilClass.convertLongToTrackTime(track.getDuration());

            queueString.append(String.format(trackFormat, trackNumber, track.getInfo().author, track.getInfo().title, "`[" + duration + "]`", track.getUserData(User.class).getAsMention()));

            trackNumber++;
        }
        embed.addField("Queue List", queueString.toString(), false);

        return embed;
    }

    private static List<Button> getPaginationButtons(String userId, String guildId, int currentPage, int maxPages)
    {
        List<Button> buttons = new ArrayList<>();

        buttons.add(Button.secondary(String.format("%s:pagination:queue:left:%s:%s", userId, currentPage, guildId), Emoji.fromUnicode("⬅️")).withDisabled(maxPages == 0 || currentPage <= 1));
        buttons.add(Button.secondary(String.format("%s:pagination:queue:refresh:%s:%s", userId, currentPage, guildId), Emoji.fromUnicode("🔃")));
        buttons.add(Button.secondary(String.format("%s:pagination:queue:right:%s:%s", userId, currentPage, guildId), Emoji.fromFormatted("➡️")).withDisabled(currentPage >= maxPages));

        return buttons;
    }

    public static void paginate(ButtonInteractionEvent event, String[] args)
    {
        event.deferEdit().queue();

        int pageNum;
        switch(args[3].toLowerCase())
        {
            case "left" -> pageNum = Integer.parseInt(args[4]) - 1;
            case "right" -> pageNum = Integer.parseInt(args[4]) + 1;
            default -> pageNum = Integer.parseInt(args[4]);
        }

        LinkedList<AudioTrack> queue = PlayerManager.getInstance().getMusicManager(event.getGuild()).getScheduler().getQueue();
        AudioTrack playingTrack = PlayerManager.getInstance().getMusicManager(event.getGuild()).getScheduler().getPlayer().getPlayingTrack();
        int maxPages = getMaxPages(queue);

        if(pageNum < 1)
            pageNum = 1;

        if(pageNum > maxPages)
            pageNum = maxPages;

        EmbedBuilder embed = printEmbed(queue, playingTrack, pageNum);
        event.getHook().editOriginalEmbeds(embed.build()).setComponents(ActionRow.of(getPaginationButtons(event.getUser().getId(), args[5], pageNum, maxPages))).queue();
    }
}