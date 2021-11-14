package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import music.LoopState;
import music.PlayerManager;
import music.TrackScheduler;
import net.dv8tion.jda.api.EmbedBuilder;

public class Repeat extends Command
{
    public Repeat()
    {
        this.name = "repeat";
        this.aliases = new String[] {"loop"};
        this.help = "Repeat the currently playing song or current queue.";
        this.cooldown = 3;
    }

    @Override
    protected void execute(CommandEvent commandEvent)
    {
        String[] args = commandEvent.getArgs().split("\\s+");
        TrackScheduler scheduler = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).getScheduler();
        EmbedBuilder embed = new EmbedBuilder().setColor(0x409df5);

        if(args[0].equalsIgnoreCase("song"))
        {
            scheduler.setLoopState(LoopState.SONG);
            embed.setDescription(":white_check_mark: I will now repeat the **current playing song**.");
        }

        else if(args[0].equalsIgnoreCase("queue"))
        {
            scheduler.setLoopState(LoopState.QUEUE);
            embed.setDescription(":white_check_mark: I will now repeat the **current queue**.");
        }

        else if(args[0].equalsIgnoreCase("off"))
        {
            scheduler.setLoopState(LoopState.DISABLED);
            embed.setDescription(":white_check_mark: The repeat function is now **disabled**.");
        }

        else
        {
            switch (scheduler.getLoopState())
            {
                case DISABLED -> embed.setDescription("I am not currently **not repeating anything**.");
                case SONG     -> embed.setDescription("I am repeating the **current playing song**.");
                case QUEUE    -> embed.setDescription("I am repeating the **current state of the queue**.");
            }

            embed.appendDescription("\n\nIf you'd like to change the repeat state, please choose one of the following options:");
            embed.addField("!repeat song", "Repeats the currently playing song.", false);
            embed.addField("!repeat queue", "Repeats the currently state of the queue. " +
                    "After a song has played, it will be re-added to the end of the queue.", false);
            embed.addField("!repeat off", "Disables the repeat function.", false);
        }

        commandEvent.reply(embed.build());
    }
}
