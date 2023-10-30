package commands;

import java.util.LinkedList;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import music.GuildMusicManager;
import music.PlayerManager;
import music.TrackScheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import utils.ConvertLong;
import utils.Statics;
import utils.ThumbnailGrabber;

public class NowPlaying extends Command
{
    public NowPlaying()
    {
        this.name = "nowplaying";
        this.aliases = new String[]{ "np" };
        this.help = "Displays info about the current playing track.";
        this.cooldown = 3;

        this.category = new Category("Tools");
    }

    @Override
    protected void execute(CommandEvent commandEvent)
    {
        GuildMusicManager manager = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild());
        TrackScheduler scheduler = manager.getScheduler();
        AudioTrack track = manager.getScheduler().getPlayer().getPlayingTrack();

        if(track == null)
            commandEvent.reply(":x: | There is no track playing!");

        else
        {
            LinkedList<AudioTrack> queue = manager.getScheduler().getQueue();

            AudioTrackInfo trackInfo = track.getInfo();
            EmbedBuilder embed = new EmbedBuilder().setColor(Statics.EMBED_COLOR);

            embed.setTitle("Now Playing", trackInfo.uri)
                 .addField("Title", trackInfo.title, true);

            if(track.getInfo().isStream)
                embed.addField("Time Elapsed", String.valueOf(ConvertLong.convertLongToTrackTime(track.getPosition())), true);
            else
            {
                embed.addField("Length", String.format("`%s` / `%s`",
                    ConvertLong.convertLongToTrackTime(track.getPosition()),
                    ConvertLong.convertLongToTrackTime(track.getDuration())), true);
            }

            embed.addField("Requested by", track.getUserData(User.class).getAsMention(), true)
                 .setThumbnail(ThumbnailGrabber.getThumbnail(track))
                 .addField("Size of Queue", String.valueOf(queue.size()), true)
                 .addField("Repeat State", scheduler.getLoopState().toString(), true);

            AudioTrack nextTrack = manager.getScheduler().getQueue().peek();

            if(nextTrack != null)
                embed.addField("Next Track", nextTrack.getInfo().title, true);
            else
                embed.addField("Next Track", "*None*", true);

            commandEvent.reply(embed.build());
        }
    }
}
