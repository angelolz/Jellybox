package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import music.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;

public class Stop extends Command
{
    public Stop()
    {
        this.name = "stop";
        this.help = "Stops any current track.";
        this.cooldown = 3;

        this.category = new Category("Player");
    }

    @Override
    protected void execute(CommandEvent commandEvent)
    {
        GuildVoiceState selfVoiceState = commandEvent.getSelfMember().getVoiceState();
        GuildVoiceState userVoiceState = commandEvent.getMember().getVoiceState();

        if(!selfVoiceState.inAudioChannel())
            commandEvent.reply(":x: | I'm not in a voice channel!");

        else if(!userVoiceState.inAudioChannel())
            commandEvent.reply(":x: | You need to be in a voice channel to use this command!");

        else if(selfVoiceState.inAudioChannel() && !userVoiceState.getChannel().equals(selfVoiceState.getChannel()))
            commandEvent.reply(":x: | You need to be in the same voice channel as me for this command to work!");

        else
        {
            AudioPlayer player = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).getScheduler().getPlayer();
            
            if(player.getPlayingTrack() != null)
            {
                player.getPlayingTrack().setPosition(0);
                player.setPaused(true);
                commandEvent.reply(":stop_button: | The currently playing track has been stopped.");
            }

            else
                commandEvent.reply(":x: | There is no track playing!");
        }
    }
}