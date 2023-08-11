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
        this.aliases = new String[]{ "p" };
        this.arguments = "[url or query]";
        this.help = "Plays a track from a URL or search query.";
        this.cooldown = 3;

        this.category = new Category("Player");
    }

    @Override
    protected void execute(CommandEvent commandEvent)
    {
        GuildVoiceState selfVoiceState = commandEvent.getSelfMember().getVoiceState();
        GuildVoiceState userVoiceState = commandEvent.getMember().getVoiceState();

        if(!userVoiceState.inAudioChannel())
            commandEvent.reply(":x: | You need to be in a voice channel to use this command!");

        else
        {
            AudioPlayer player = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).getScheduler().getPlayer();
            LinkedList<AudioTrack> queue = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).getScheduler().getQueue();

            //if bot is in a different channel
            if(selfVoiceState.inAudioChannel() && !userVoiceState.getChannel().equals(selfVoiceState.getChannel()))
            {
                commandEvent.reply(":x: | I'm already in another voice channel!");
                return;
            }

            //if bot is not in voice channel
            else if(!selfVoiceState.inAudioChannel())
            {
                commandEvent.getGuild().getAudioManager().setSelfDeafened(true); //this is for privacy reasons and saves on bandwidth
                commandEvent.getGuild().getAudioManager().openAudioConnection(userVoiceState.getChannel());
                commandEvent.replyFormatted(":loud_sound: | Connecting to **%s**!", userVoiceState.getChannel().getName());

                PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).setNotifChannel(commandEvent.getTextChannel());
                PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).getTimer().startTimer();
            }

            if(!commandEvent.getMessage().getAttachments().isEmpty())
                PlayerManager.getInstance().loadAndPlay(commandEvent.getTextChannel(), commandEvent.getAuthor(), commandEvent.getMessage().getAttachments());
            else
            {
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
                            commandEvent.reply(":x: | There's already a track playing! If you want to add a track to the queue, " +
                                "please give me a search query or URL!");
                        }

                        else if(queue.isEmpty())
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
                    String query;
                    if(commandEvent.getArgs().charAt(0) == '<' && commandEvent.getArgs().endsWith(">"))
                        query = commandEvent.getArgs().substring(1, commandEvent.getArgs().length() - 1);
                    else
                        query = commandEvent.getArgs();

                    if(URLUtils.isURI(query))
                        PlayerManager.getInstance().loadAndPlay(commandEvent.getTextChannel(), commandEvent.getAuthor(), query);
                    else
                        PlayerManager.getInstance().loadAndPlay(commandEvent.getTextChannel(), commandEvent.getAuthor(), "ytmsearch: " + query);

                    if(player.isPaused())
                        commandEvent.reply(":pause_button: | The player is still paused! If you want to resume playback, then type `!p` or `!play`!");
                }
            }
        }
    }
}
