package commands;

import java.util.LinkedList;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import music.GuildMusicManager;
import music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import utils.ConvertLong;
import utils.ThumbnailGrabber;

public class NowPlaying extends Command
{
    public NowPlaying()
    {
        this.name = "nowplaying";
        this.aliases = new String[] {"np"};
        this.help = "Displays info about the current playing track.";
        this.cooldown = 3;

        this.category = new Category("Tools");
    }

    @Override
    protected void execute(CommandEvent commandEvent)
    {
        GuildMusicManager manager = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild());
        AudioPlayer player = manager.getScheduler().getPlayer();
        AudioTrack track = player.getPlayingTrack();
        LinkedList<AudioTrack> queue = manager.getScheduler().getQueue();

        if(track == null)
            commandEvent.reply(":x: | There is no track playing!");

        else
        {
            AudioTrackInfo trackInfo = track.getInfo();
            EmbedBuilder embed = new EmbedBuilder().setColor(0x409df5);

            embed.setTitle("Now Playing", trackInfo.uri);
            embed.addField("Artist", trackInfo.author, true);
            embed.addField("Title", trackInfo.title, true);
            if(track.getInfo().isStream)
                embed.addField("Time Elapsed", String.valueOf(ConvertLong.convertLongToTrackTime(track.getPosition())), true);
            else
            {
                embed.addField("Length",  String.format("`%s` / `%s`",
                    ConvertLong.convertLongToTrackTime(track.getPosition()),
                    ConvertLong.convertLongToTrackTime(track.getDuration())), true);
            }
            embed.addField("Requested by", track.getUserData(User.class).getAsMention(), true);
            embed.setThumbnail(ThumbnailGrabber.getThumbnail(track));
            embed.addField("Size of Queue", String.valueOf(queue.size()), true);

            AudioTrack nextTrack = manager.getScheduler().getQueue().peek();

            if(nextTrack != null)
                embed.addField("Next Track",nextTrack.getInfo().title, true);
            else
                embed.addField("Next Track", "*None*", true);

            commandEvent.reply(embed.build());
        }
    }
}
