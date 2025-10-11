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
import utils.Statics;
import utils.UtilClass;

public class NowPlaying extends Command {

    public NowPlaying() {
        this.name = "nowplaying";
        this.aliases = new String[]{ "np" };
        this.help = "Displays info about the current playing track.";
        this.cooldown = 3;

        this.category = new Category("Player");
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        GuildMusicManager manager = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild());
        TrackScheduler scheduler = manager.getScheduler();
        AudioTrack track = manager.getScheduler().getPlayer().getPlayingTrack();

        if(track == null) {
            commandEvent.replyError("There is no track playing!");
            return;
        }

        LinkedList<AudioTrack> queue = manager.getScheduler().getQueue();

        AudioTrackInfo trackInfo = track.getInfo();
        AudioTrack nextTrack = manager.getScheduler().getQueue().peek();
        EmbedBuilder embed = new EmbedBuilder().setColor(Statics.EMBED_COLOR)
            .setTitle("Now Playing")
            .setThumbnail(track.getInfo().artworkUrl)
            .addField("Current Track", UtilClass.getTrackInfoForEmbed(trackInfo), true)
                                               .addField("Length", String.format("`%s` / `%s`", UtilClass.convertLongToTrackTime(track.getPosition()),
                UtilClass.convertLongToTrackTime(track.getDuration())), true)
            .addField("Requested by", track.getUserData(User.class).getAsMention(), true)


            .addField("Next Track", nextTrack != null ? UtilClass.getTrackInfoForEmbed(nextTrack.getInfo()) : "*None*", true)
            .addField("Queue Size", queue.isEmpty() ? "*Empty*" : String.format("**%s** tracks%n`%s` playtime",
                queue.size(), UtilClass.convertLongToTrackTime(queue.stream()
                .mapToLong(AudioTrack::getDuration)
                .sum())), true)
            .addField("Repeat State", scheduler.getLoopState().toString(), true);

        commandEvent.reply(embed.build());
    }
}
