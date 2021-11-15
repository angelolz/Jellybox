package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import music.LoopState;
import music.PlayerManager;
import music.TrackScheduler;
import net.dv8tion.jda.api.entities.GuildVoiceState;

public class Leave extends Command
{
    public Leave()
    {
        this.name = "leave";
        this.help = "Leaves the currently joined voice channel.";
        this.cooldown = 3;
    }

    @Override
    protected void execute(CommandEvent commandEvent)
    {
        GuildVoiceState selfVoiceState = commandEvent.getSelfMember().getVoiceState();

        if(!selfVoiceState.inVoiceChannel())
            commandEvent.reply(":x: | I'm not in a voice channel!");

        else
        {
            selfVoiceState.getGuild().getAudioManager().closeAudioConnection();

            TrackScheduler scheduler = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).getScheduler();
            scheduler.getPlayer().stopTrack();
            scheduler.getQueue().clear();
            scheduler.getPlayer().setPaused(false);
            scheduler.setLoopState(LoopState.DISABLED);

            commandEvent.reply(":wave: Goodbye!");
        }
    }
}
