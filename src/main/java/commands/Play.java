package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import music.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import structure.MusicTrack;
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
            commandEvent.reply(":x: | You need to be in a voice channel to use this command!");

        else
        {
            AudioPlayer player = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).getScheduler().getPlayer();
            LinkedList<MusicTrack> queue = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).getScheduler().getQueue();

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

                PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).setNotifChannel(commandEvent.getTextChannel());
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
                    if(player.getPlayingTrack() != null)
                    {
                        commandEvent.reply(":x: | There's already a song playing! If you want to add a song to the queue, " +
                            "please give me a search query or URL!");
                    }

                    else if(queue.isEmpty())
                        commandEvent.reply(":x: | There's nothing to play because the queue is empty!");

                    else
                    {
                        player.playTrack(queue.poll().getTrack());
                        commandEvent.reply(":arrow_forward: | Resumed playback!");
                    }
                }
            }

            else
            {
                if(!URLUtils.isURI(commandEvent.getArgs()))
                    PlayerManager.getInstance().loadAndPlay(commandEvent.getTextChannel(), commandEvent.getAuthor(), "ytsearch: " + commandEvent.getArgs());

                else
                    PlayerManager.getInstance().loadAndPlay(commandEvent.getTextChannel(), commandEvent.getAuthor(), commandEvent.getArgs());
            }
        }
    }
}
