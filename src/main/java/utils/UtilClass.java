package utils;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.wrapper.spotify.model_objects.specification.Image;
import com.wrapper.spotify.model_objects.specification.Track;
import main.Jukebox;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilClass
{
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

    public static boolean isURI(String urlString)
    {
        try
        {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        }

        catch(Exception e)
        {
            return false;
        }
    }

    public static String readURL(String string) throws IOException
    {
        URL url = new URL(string);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
        con.connect();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream())))
        {
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while((read = reader.read(chars)) != -1)
            {
                buffer.append(chars, 0, read);
            }

            return buffer.toString();
        }
    }

    public static String getThumbnail(AudioTrack track)
    {
        try
        {
            switch(track.getSourceManager().getSourceName())
            {
                case "youtube" -> { return String.format("https://img.youtube.com/vi/%s/default.jpg", track.getIdentifier()); }
                case "soundcloud" -> { return null; } // Unable to sign up for SoundCloud API
                case "twitch" -> { return Jukebox.getTwitchApi().getURL(track.getInfo().author); }
                case "spotify" ->
                {
                    final String PATTERN = "^(?:http://|https://)?[a-z]+.spotify.com/[a-z]+/([a-z0-9]+).*$";
                    final Pattern SPOTIFY_REGEX = Pattern.compile(PATTERN);
                    Matcher m = SPOTIFY_REGEX.matcher(track.getInfo().uri);
                    if(m.matches())
                    {
                        Track spotifyTrack = Jukebox.getSpotifyApi().getTrack(m.group(1)).build().execute();
                        Image[] images = spotifyTrack.getAlbum().getImages();
                        return images[0].getUrl();
                    }

                    else
                        return null;
                }
                default ->
                {
                    Jukebox.getLogger().warn("Unknown source: {}", track.getSourceManager().getSourceName());
                    return null;
                }
            }
        }

        catch(Exception e)
        {
            Jukebox.getLogger().error("Error getting thumbnail: {}", e.getMessage());
            return null;
        }
    }

    public static boolean checkInvalidVoiceState(CommandEvent commandEvent, GuildVoiceState selfVoiceState, GuildVoiceState userVoiceState)
    {
        if(!selfVoiceState.inAudioChannel())
        {
            commandEvent.reply(":x: | I'm not in a voice channel!");
            return true;
        }

        if(!userVoiceState.inAudioChannel())
        {
            commandEvent.reply(":x: | You need to be in a voice channel to use this command!");
            return true;
        }

        if(selfVoiceState.inAudioChannel() && !userVoiceState.getChannel().equals(selfVoiceState.getChannel()))
        {
            commandEvent.reply(":x: | You need to be in the same voice channel as me for this command to work!");
            return true;
        }

        return false;
    }
}
