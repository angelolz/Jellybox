package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import music.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import utils.Statics;
import utils.UtilClass;

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
        {
            commandEvent.replyError("You need to be in a voice channel to use this command!");
            return;
        }

        AudioPlayer player = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).getScheduler().getPlayer();
        LinkedList<AudioTrack> queue = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).getScheduler().getQueue();

        if(!botJoinedVoiceChannel(commandEvent, selfVoiceState, userVoiceState))
            return;

        if(commandEvent.getArgs().isEmpty() && commandEvent.getMessage().getAttachments().isEmpty())
            unpauseBot(commandEvent, player, queue);
        else
            loadTrack(commandEvent, player);
    }

    private boolean botJoinedVoiceChannel(CommandEvent commandEvent, GuildVoiceState selfVoiceState, GuildVoiceState userVoiceState)
    {
        if(selfVoiceState.inAudioChannel() && !userVoiceState.getChannel().equals(selfVoiceState.getChannel()))
        {
            commandEvent.replyError("I'm already in another voice channel!");
            return false;
        }

        if(!selfVoiceState.inAudioChannel())
            Join.joinVoiceChannel(commandEvent, userVoiceState);

        return true;
    }

    private static void unpauseBot(CommandEvent commandEvent, AudioPlayer player, LinkedList<AudioTrack> queue)
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
                commandEvent.replyError("There's already a track playing! If you want to add a track to the queue, " +
                    "please give me a search query or URL!");
                return;
            }

            if(queue.isEmpty())
            {
                commandEvent.replyError("There's nothing to play because the queue is empty! Add a track by giving a search query or URL.");
                return;
            }

            player.playTrack(queue.poll());
            commandEvent.reply(":arrow_forward: | Resumed playback!");
        }
    }

    private static void loadTrack(CommandEvent commandEvent, AudioPlayer player)
    {
        if(PlayerManager.getInstance().getMusicManager(commandEvent.getGuild()).getScheduler().getQueue().size() >= Statics.MAX_QUEUE_ITEMS)
        {
            commandEvent.replyFormatted("❌ | Your request was not loaded due to the queue reaching the maximum size of **%d** tracks.", Statics.MAX_QUEUE_ITEMS);
            return;
        }

        if(player.isPaused())
            commandEvent.reply(":pause_button: | The player is still paused! If you want to resume playback, then type `!p` or `!play`!");

        //attachments get priority
        if(!commandEvent.getMessage().getAttachments().isEmpty())
        {
            PlayerManager.getInstance().loadAndPlay(commandEvent.getTextChannel(), commandEvent.getAuthor(), commandEvent.getMessage().getAttachments());
            return;
        }

        //there's no attachments, it's either a query or url
        String query = commandEvent.getArgs().startsWith("<") && commandEvent.getArgs().endsWith(">")
            ? commandEvent.getArgs().substring(1, commandEvent.getArgs().length() - 1)
            : commandEvent.getArgs();

        if(UtilClass.isURI(query))
            PlayerManager.getInstance().loadAndPlay(commandEvent.getTextChannel(), commandEvent.getAuthor(), query);
        else
            PlayerManager.getInstance().loadAndPlay(commandEvent.getTextChannel(), commandEvent.getAuthor(), "ytmsearch: " + query);
    }
}
