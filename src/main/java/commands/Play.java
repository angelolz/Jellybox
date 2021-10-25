package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import music.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import utils.URLUtils;

import java.util.LinkedList;

public class Play extends Command
{
    public Play()
    {
        this.name = "play";
        this.aliases = new String[] {"p"};
        this.help = "Plays a song from a URL or search query.";
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
            AudioPlayer player = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).getScheduler().getPlayer();
            LinkedList<AudioTrack> queue = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).getScheduler().getQueue();

            //if bot is in a different channel
            if(selfVoiceState.inVoiceChannel() && !userVoiceState.getChannel().equals(selfVoiceState.getChannel()))
            {
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

            if(commandEvent.getArgs().isEmpty())
            {
                if(player.isPaused())
                {
                    player.setPaused(false);
                    commandEvent.reply(":arrow_forward: | Resumed playback!");
                }

                else
                {
                    if(queue.isEmpty())
                        commandEvent.reply(":x: | There's nothing to play because the queue is empty!");

                    else
                    {
                        player.playTrack(queue.poll());
                        commandEvent.reply(":arrow_forward: | Resumed playback!");
                    }
                }
            }

            else
            {
                if(!URLUtils.isURI(commandEvent.getArgs()))
                    PlayerManager.getInstance().loadAndPlay(commandEvent.getTextChannel(), "ytsearch: " + commandEvent.getArgs());
                else
                    PlayerManager.getInstance().loadAndPlay(commandEvent.getTextChannel(), commandEvent.getArgs());
            }
        }
    }
}
