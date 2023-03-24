package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import music.PlayerManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;

public class Join extends Command
{
    public Join()
    {
        this.name = "join";
        this.help = "Joins the same voice channel as you.";
        this.cooldown = 3;

        this.category = new Category("Bot");
    }

    @Override
    protected void execute(CommandEvent commandEvent)
    {
        GuildVoiceState selfVoiceState = commandEvent.getSelfMember().getVoiceState();
        GuildVoiceState userVoiceState = commandEvent.getMember().getVoiceState();

        if(selfVoiceState.inAudioChannel())
            commandEvent.reply(":x: | I'm already in a voice channel!");

        else
        {
            if(!userVoiceState.inAudioChannel())
                commandEvent.reply(":x: | You must be in a voice channel in order to use this command!");

            else
            {
                if(!commandEvent.getSelfMember().hasPermission(userVoiceState.getChannel(), Permission.VOICE_CONNECT))
                    commandEvent.reply(":x: | I don't have permission to connect to the voice channel!");

                else
                {
                    commandEvent.getGuild().getAudioManager().setSelfDeafened(true); //this is for privacy reasons and saves on bandwidth
                    commandEvent.getGuild().getAudioManager().openAudioConnection(userVoiceState.getChannel());

                    PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).setNotifChannel(commandEvent.getTextChannel());
                    PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).getTimer().startTimer();

                    commandEvent.replyFormatted(":loud_sound: | Connecting to **%s**!", userVoiceState.getChannel().getName());
                }
            }
        }
    }
}
