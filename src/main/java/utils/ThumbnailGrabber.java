package utils;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.wrapper.spotify.model_objects.specification.Image;
import com.wrapper.spotify.model_objects.specification.Track;
import main.Jukebox;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThumbnailGrabber
{
    public static String getThumbnail(AudioTrack track)
    {
        try
        {
            switch(track.getSourceManager().getSourceName())
            {
                case "youtube":
                    return String.format("https://img.youtube.com/vi/%s/default.jpg", track.getIdentifier());
                case "spotify":
                    final String PATTERN = "^(?:http://|https://)?[a-z]+.spotify.com/[a-zA-z]+/([a-zA-z0-9]+).*$";
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
                case "soundcloud":
                    //unable to sign up for soundcloud api
                    return null;
                case "twitch":
                    return Jukebox.getTwitchApi().getURL(track.getInfo().author);
            }

            return null;
        }

        catch(Exception e)
        {
            Jukebox.getLogger().error("Error getting thumbnail: {}", e.getMessage());
            return null;
        }
    }
}
