package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import main.Jukebox;
import music.LoopState;
import music.PlayerManager;
import music.TrackScheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import utils.Statics;

public class Repeat extends Command
{
    public Repeat()
    {
        this.name = "repeat";
        this.aliases = new String[]{ "loop" };
        this.help = "Repeat the currently playing track or current queue.";
        this.cooldown = 3;

        this.category = new Category("Player");
    }

    @Override
    protected void execute(CommandEvent commandEvent)
    {
        String[] args = commandEvent.getArgs().split("\\s+");
        TrackScheduler scheduler = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).getScheduler();
        EmbedBuilder embed = new EmbedBuilder().setColor(Statics.EMBED_COLOR);

        if(args[0].equalsIgnoreCase("track"))
        {
            scheduler.setLoopState(LoopState.TRACK);
            embed.setDescription(":white_check_mark: | I will now repeat the **current track**.");
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
            switch(scheduler.getLoopState())
            {
                case DISABLED -> embed.setDescription("I am not currently **not repeating anything**.");
                case TRACK -> embed.setDescription("I am repeating the **current track**.");
                case QUEUE -> embed.setDescription("I am repeating the **current state of the queue**.");
            }

            embed.appendDescription("\n\nIf you'd like to change the repeat state, please choose one of the following options:")
                 .addField(String.format("%srepeat/loop track", Jukebox.getPrefix()), "Repeats the current track.", false)
                 .addField(String.format("%srepeat/loop queue", Jukebox.getPrefix()), "Repeats the currently state of the queue. " +
                     "After a track has been played, it will be re-added to the end of the queue.", false)
                 .addField(String.format("%srepeat/loop off", Jukebox.getPrefix()), "Disables the repeat function.", false);
        }

        commandEvent.reply(embed.build());
    }
}
