package commands;
import java.util.Collections;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import music.PlayerManager;
import music.TrackScheduler;
import net.dv8tion.jda.api.entities.GuildVoiceState;

public class Shuffle extends Command
{
    public Shuffle()
    {
        this.name = "Shuffle";
        this.help = "Shuffles the tracks that are in the queue (if any).";
        this.cooldown = 3;
    }

    @Override
    protected void execute(CommandEvent commandEvent)
    {
        GuildVoiceState userVoiceState = commandEvent.getMember().getVoiceState();
        GuildVoiceState selfVoiceState = commandEvent.getSelfMember().getVoiceState();

        if(!selfVoiceState.inVoiceChannel())
            commandEvent.reply(":x: | I'm not in a voice channel!");

        else if(!userVoiceState.inVoiceChannel())
            commandEvent.reply(":x: | You need to be in a voice channel to use this command!");

        else if(selfVoiceState.inVoiceChannel() && !userVoiceState.getChannel().equals(selfVoiceState.getChannel()))
            commandEvent.reply(":x: | You need to be in the same voice channel as me for this command to work!");

        else
        {
            TrackScheduler scheduler  = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).getScheduler();

            if(scheduler.getQueue().size() == 0)
                commandEvent.reply(":x: | There are no tracks in the queue to shuffle!");

            else
            {
                Collections.shuffle(scheduler.getQueue());
                commandEvent.reply(":white_check_mark: | The queue has been shuffled.");
            }
        }
    }
}
