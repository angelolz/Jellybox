package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import music.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;

public class Pause extends Command
{
    public Pause()
    {
        this.name = "pause";
        this.help = "Pauses the currently playing song at its position";
        this.cooldown = 3;
    }

    @Override
    protected void execute(CommandEvent commandEvent)
    {
        GuildVoiceState selfVoiceState = commandEvent.getSelfMember().getVoiceState();
        GuildVoiceState userVoiceState = commandEvent.getMember().getVoiceState();

        if(!selfVoiceState.inVoiceChannel())
            commandEvent.reply(":x: | I'm not even in a voice channel!");

        else if(!userVoiceState.inVoiceChannel())
            commandEvent.reply(":x: | You need to be in a voice channel to use this command!");

        else if(selfVoiceState.inVoiceChannel() && !userVoiceState.getChannel().equals(selfVoiceState.getChannel()))
            commandEvent.reply(":x: | You need to be in the same voice channel as me for this command to work!");

        else
        {
            PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).getScheduler().getPlayer().setPaused(true);
            commandEvent.reply(":pause_button: | Paused playback!");
        }
    }
}
