package utils;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.User;

public class UtilClass
{
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }
    public static String convertLongToTrackTime(long length)
    {
        int hours = (int) (length / (1000 * 60 * 60));
        int minutes = (int) ((length / (1000 * 60)) % 60);
        int seconds = (int) ((length / 1000) % 60);

        if(hours > 0)
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        else
            return String.format("%02d:%02d", minutes, seconds);
    }

    public static String convertLongToDaysLength(long ms)
    {
        int days;
        int hours;
        int minutes;
        int seconds;
        StringBuilder result = new StringBuilder();

        seconds = (int) ((ms / 1000) % 60);
        minutes = (int) ((ms / (1000 * 60)) % 60);
        hours = (int) ((ms / (1000 * 60 * 60)) % 24);
        days = (int) (ms / (1000 * 60 * 60 * 24));

        if(days > 0)
            result.append(String.format("%d days, %02d:%02d:%02d", days, hours, minutes, seconds));

        else
            result.append(String.format("%02d:%02d:%02d", hours, minutes, seconds));

        return result.toString();
    }

    public static boolean checkInvalidVoiceState(CommandEvent commandEvent, GuildVoiceState selfVoiceState, GuildVoiceState userVoiceState)
    {
        if(!selfVoiceState.inAudioChannel())
        {
            commandEvent.replyError("I'm not in a voice channel!");
            return true;
        }

        if(!userVoiceState.inAudioChannel())
        {
            commandEvent.replyError("You need to be in a voice channel to use this command!");
            return true;
        }

        if(selfVoiceState.inAudioChannel() && !userVoiceState.getChannel().equals(selfVoiceState.getChannel()))
        {
            commandEvent.replyError("You need to be in the same voice channel as me for this command to work!");
            return true;
        }

        return false;
    }

    public static String getTrackInfoForEmbed(AudioTrackInfo trackInfo) {
        return String.format("**%s**%n%s%n%s", trackInfo.title, trackInfo.author, trackInfo.isrc == null ? "" : " *" + trackInfo.isrc + "*");
    }

    public static EmbedBuilder getNowPlayingEmbed(AudioTrack track) {
        return new EmbedBuilder().setColor(Statics.EMBED_COLOR)
            .setTitle("Now Playing")
            .setThumbnail(track.getInfo().artworkUrl)
            .setDescription(UtilClass.getTrackInfoForEmbed(track.getInfo()))
            .addField("Length",  UtilClass.convertLongToTrackTime(track.getInfo().length), true)
            .addField("Added by", track.getUserData(User.class).getAsMention(), true);
    }
}
