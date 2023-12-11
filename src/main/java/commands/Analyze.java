package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import de.androidpit.colorthief.ColorThief;
import main.Jukebox;
import music.sources.spotify.SpotifyAudioSourceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.Track;
import utils.UtilClass;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

public class Analyze extends Command
{
    public Analyze()
    {
        this.name = "analyze";
        this.help = "Gives you interesting information about a track. (Spotify tracks only)";
        this.category = new Category("Tools");
    }

    @Override
    protected void execute(CommandEvent event)
    {
        if(event.getArgs().isEmpty())
        {
            event.replyError("You need to provide a Spotify track link.");
            return;
        }

        SpotifyAudioSourceManager spotifyAudioSourceManager = new SpotifyAudioSourceManager();
        Matcher m = spotifyAudioSourceManager.getSpotifyPattern().matcher(event.getArgs());

        if(!m.find())
        {
            event.replyError("This link is not a Spotify track link.");
            return;
        }

        if(!m.group(1).equalsIgnoreCase("track"))
        {
            event.replyError("Only spotify track links are supported.");
            return;
        }

        try
        {
            Track track = Jukebox.getSpotifyApi().getTrack(m.group(2)).build().execute();
            AudioFeatures audioFeatures = Jukebox.getSpotifyApi().getAudioFeaturesForTrack(m.group(2)).build().execute();
            List<String> artistsIds = Arrays.stream(track.getArtists()).map(ArtistSimplified::getId).toList();
            String[] artistsStringArr = String.join(",", artistsIds).split(",");
            Artist[] artists = Jukebox.getSpotifyApi().getSeveralArtists(artistsStringArr).build().execute();
            EmbedBuilder embed = new EmbedBuilder();

            //256 = max length of embed title
            //16 = length of "Track Analysis: "
            //3 = ellipsis
            String title = track.getName().length() > (256 - 16) ? track.getName().substring(0, 256 - 16 - 3) + "..." : track.getName();
            embed.setColor(Color.black)
                 .setTitle("Track Analysis: " + title)
                 .setThumbnail(track.getAlbum().getImages()[0].getUrl())
                 .setColor(getAlbumColor(track.getAlbum().getImages()[0].getUrl()))

                 .addField("Length", UtilClass.convertLongToTrackTime(track.getDurationMs()), true)
                 .addField("Tempo", audioFeatures.getTempo() + " BPM", true)
                 .addField("Loudness", audioFeatures.getLoudness() + " dB", true)

                 .addField("Key", getKey(audioFeatures.getKey(), audioFeatures.getMode().getType()), true)
                 .addField("Time Signature", audioFeatures.getTimeSignature() + "/4", true)
                 .addBlankField(true)

                 .addField("Artist(s) Genres:", getGenres(artists), false)

                 .addField("Popularity", track.getPopularity() + "%", true)
                 .addField("Happiness", toPercentage(audioFeatures.getValence()), true)
                 .addField("Danceability", toPercentage(audioFeatures.getDanceability()), true)

                 .addField("Energy", toPercentage(audioFeatures.getEnergy()), true)
                 .addField("Acousticness", toPercentage(audioFeatures.getAcousticness()), true)
                 .addField("Instrumentalness", toPercentage(audioFeatures.getInstrumentalness()), true)

                 .addField("Liveness", toPercentage(audioFeatures.getLiveness()), true)
                 .addField("Speechiness", toPercentage(audioFeatures.getSpeechiness()), true)
                 .addBlankField(true);

            event.reply(embed.build());
        }

        catch(Exception e)
        {
            event.replyError("There was an error retrieving the analysis for this track.");
            Jukebox.getLogger().error("Error getting analysis: {}", e.getMessage(), e);
        }
    }

    private String getKey(int key, int mode)
    {
        String val;

        if(key == -1) return "*Unknown*";

        switch(key)
        {
            case 0 -> val = "C";
            case 1 -> val = "C♯ / D♭";
            case 2 -> val = "D";
            case 3 -> val = "D♯ / E♭";
            case 4 -> val = "E";
            case 5 -> val = "F";
            case 6 -> val = "F♯ / G♭";
            case 7 -> val = "G";
            case 8 -> val = "G♯ / A♭";
            case 9 -> val = "A";
            case 10 -> val = "A♯ / B♭";
            case 11 -> val = "B";
            default -> { return "*Unknown*"; }
        }

        if(mode == 0)
            val += " minor";
        else
            val += " major";

        return val;
    }

    private static String toPercentage(float n)
    {
        return String.format("%.2f", n * 100) + "%";
    }

    private static String getGenres(Artist[] artists)
    {
        StringBuilder sb = new StringBuilder();

        for(Artist artist : artists)
        {
            String genres = String.join(", ", artist.getGenres());
            sb.append("**").append(artist.getName()).append("**: ").append(genres.isEmpty() ? "*Unknown*" : genres).append("\n");
        }

        return sb.toString();
    }

    private static Color getAlbumColor(String albumImgUrl) throws IOException
    {
        BufferedImage img = ImageIO.read(new URL(albumImgUrl));
        int[] ints = ColorThief.getColor(img);
        return new Color(ints[0], ints[1], ints[2]);
    }
}
