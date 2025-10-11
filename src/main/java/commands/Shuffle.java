package commands;

import java.util.Collections;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import music.PlayerManager;
import music.TrackScheduler;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import utils.UtilClass;

public class Shuffle extends Command {

    public Shuffle() {
        this.name = "Shuffle";
        this.help = "Shuffles the tracks that are in the queue (if any).";
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

        if(scheduler.getQueue().isEmpty())
            commandEvent.replyError("There are no tracks in the queue to shuffle!");

        else {
            Collections.shuffle(scheduler.getQueue());
            commandEvent.reply("✅ | The queue has been shuffled.");
        }
    }
}
