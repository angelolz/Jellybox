package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import music.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import utils.UtilClass;

public class Pause extends Command
{
    public Pause()
    {
        this.name = "pause";
        this.help = "Pauses the current track.";
        this.cooldown = 3;

        this.category = new Category("Player");
    }

    @Override
    protected void execute(CommandEvent commandEvent)
    {
        GuildVoiceState selfVoiceState = commandEvent.getSelfMember().getVoiceState();
        GuildVoiceState userVoiceState = commandEvent.getMember().getVoiceState();

        if(UtilClass.checkInvalidVoiceState(commandEvent, selfVoiceState, userVoiceState)) return;

        AudioPlayer player = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).getScheduler().getPlayer();

        if(player.getPlayingTrack() != null)
        {
            if(player.isPaused())
            {
                commandEvent.replyError("I'm already paused! Type `!p` or `!play` to continue playback!");
                return;
            }

            player.setPaused(true);
            commandEvent.reply(":pause_button: | Paused playback!");
        }

        else
            commandEvent.replyError("There's nothing playing!");
    }
}
