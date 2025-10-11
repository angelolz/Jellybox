package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import music.PlayerManager;
import music.TrackScheduler;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import utils.UtilClass;

public class Skip extends Command {

    public Skip() {
        this.name = "skip";
        this.help = "Skips the current track.";
        this.cooldown = 3;

        this.category = new Category("Player");
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        GuildVoiceState userVoiceState = commandEvent.getMember().getVoiceState();
        GuildVoiceState selfVoiceState = commandEvent.getSelfMember().getVoiceState();

        if(UtilClass.checkInvalidVoiceState(commandEvent, selfVoiceState, userVoiceState))
            return;

        TrackScheduler scheduler = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).getScheduler();
        if(scheduler.getPlayer().getPlayingTrack() == null) {
            commandEvent.replyError("There's no track playing!");
            return;
        }

        if(scheduler.getQueue().peek() != null)
            scheduler.nextTrack();
        else
            scheduler.getPlayer().stopTrack();

        commandEvent.reply(":next_track: | The track has been skipped.");
    }
}