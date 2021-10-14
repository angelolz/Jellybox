package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import music.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import utils.URLUtils;

import javax.print.URIException;
import java.net.URI;
import java.net.URISyntaxException;

public class Play extends Command
{
    public Play()
    {
        this.name = "play";
        this.help = "Plays a song from a URL."; //TODO improve this help lol
        this.cooldown = 3;
    }

    @Override
    protected void execute(CommandEvent commandEvent)
    {
        GuildVoiceState selfVoiceState = commandEvent.getSelfMember().getVoiceState();
        GuildVoiceState userVoiceState = commandEvent.getMember().getVoiceState();

        if(!userVoiceState.inVoiceChannel())
        {
            commandEvent.reply(":x: | You need to be in a voice channel to use this command!");
        }

        else
        {
            //if bot is in a different channel
            if(selfVoiceState.inVoiceChannel() && !userVoiceState.getChannel().equals(selfVoiceState.getChannel()))
            {
                //TODO if the bot isn't playing music, it will just transfer to another voice channel
                commandEvent.reply(":x: | I'm already in another voice channel!");
                return;
            }

            //if bot is not in voice channel
            else if(!selfVoiceState.inVoiceChannel())
            {
                commandEvent.getGuild().getAudioManager().setSelfDeafened(true); //this is for privacy reasons and saves on bandwidth
                commandEvent.getGuild().getAudioManager().openAudioConnection(userVoiceState.getChannel());
                commandEvent.replyFormatted(":loud_sound: | Connecting to **%s**!", userVoiceState.getChannel().getName());
            }

            String link = "";
            if(!URLUtils.isURI(commandEvent.getArgs()))
                PlayerManager.getInstance().loadAndPlay(commandEvent.getTextChannel(), "ytsearch: " + commandEvent.getArgs());
            else
                PlayerManager.getInstance().loadAndPlay(commandEvent.getTextChannel(), commandEvent.getArgs());
        }
    }
}
