package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import music.PlayerManager;
import music.TrackScheduler;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import org.checkerframework.checker.units.qual.C;

public class Skip extends Command
{
    public Skip()
    {
        this.name = "skip";
        this.help = "Skips the current track.";
        this.cooldown = 3;

        this.category = new Category("Player");
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
            if(scheduler.getPlayer().getPlayingTrack() == null)
                commandEvent.reply(":x: | There's no track playing!");

            else
            {
                if(scheduler.getQueue().peek() != null)
                    scheduler.nextTrack();
                else
                    scheduler.getPlayer().stopTrack();

                commandEvent.reply(":next_track: | The track has been skipped.");
            }
        }
    }
}
