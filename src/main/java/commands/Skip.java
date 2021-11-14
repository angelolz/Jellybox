package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import music.PlayerManager;
import music.TrackScheduler;
import net.dv8tion.jda.api.entities.GuildVoiceState;

public class Skip extends Command
{
    public Skip()
    {
        this.name = "skip";
        this.help = "Skips the currently playing song (if any).";
        this.cooldown = 3;
    }
    @Override
    protected void execute(CommandEvent commandEvent)
    {
        GuildVoiceState userVoiceState = commandEvent.getMember().getVoiceState();
        GuildVoiceState selfVoiceState = commandEvent.getSelfMember().getVoiceState();

        if(!selfVoiceState.inVoiceChannel())
            commandEvent.reply(":x: | I'm not even in a voice channel!");

        else if(!userVoiceState.inVoiceChannel())
            commandEvent.reply(":x: | You need to be in a voice channel to use this command!");

        else if(selfVoiceState.inVoiceChannel() && !userVoiceState.getChannel().equals(selfVoiceState.getChannel()))
            commandEvent.reply(":x: | You need to be in the same voice channel as me for this command to work!");

        else
        {
            TrackScheduler scheduler  = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).getScheduler();
            if(scheduler.getPlayer().getPlayingTrack() == null)
            {
                commandEvent.reply(":x: | There's no song playing!");
            }

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
