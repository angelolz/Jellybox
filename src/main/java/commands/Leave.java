package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import music.GuildMusicManager;
import music.LoopState;
import music.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;

public class Leave extends Command
{
    public Leave()
    {
        this.name = "leave";
        this.help = "Leaves the currently joined voice channel.";
        this.cooldown = 3;

        this.category = new Category("Bot");
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

            GuildMusicManager manager = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild());
            manager.getScheduler().getPlayer().stopTrack();
            manager.getScheduler().getQueue().clear();
            manager.getScheduler().getPlayer().setPaused(false);
            manager.getScheduler().setLoopState(LoopState.DISABLED);
            manager.resetNotifChannel();

            commandEvent.reply(":wave: Goodbye!");
        }
    }
}
