package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import music.GuildMusicManager;
import music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import structure.MusicTrack;
import utils.ConvertLong;

public class NowPlaying extends Command
{
    public NowPlaying()
    {
        this.name = "nowplaying";
        this.aliases = new String[] {"np"};
        this.cooldown = 3;
    }

    @Override
    protected void execute(CommandEvent commandEvent)
    {
        GuildMusicManager manager = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild());
        AudioPlayer player = manager.getScheduler().getPlayer();
        AudioTrack track = player.getPlayingTrack();

        if(track == null)
            commandEvent.reply(":x: | There is no song playing!");

        else
        {
            AudioTrackInfo trackInfo = track.getInfo();
            EmbedBuilder embed = new EmbedBuilder().setColor(0x409df5);

            embed.setTitle("Now Playing", trackInfo.uri);
            embed.addField("Artist", trackInfo.author, true);
            embed.addField("Title", trackInfo.title, true);
            embed.addField("Length",  String.format("`%s` / `%s`",
                ConvertLong.convertLongToTrackTime(track.getPosition()),
                ConvertLong.convertLongToTrackTime(track.getDuration())), true);
            embed.addField("Requested by", manager.getRequester().getAsMention(), true);

            MusicTrack nextTrack = manager.getScheduler().getQueue().peek();

            if(nextTrack != null)
                embed.addField("Next Song",nextTrack.getTrack().getInfo().title, true);
            else
                embed.addField("Next Song", "*None*", true);

            commandEvent.reply(embed.build());
        }
    }
}
