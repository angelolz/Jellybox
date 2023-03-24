package music.sources.spotify;

import com.neovisionaries.i18n.CountryCode;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.IPlaylistItem;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.data.artists.GetArtistsTopTracksRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import main.Jukebox;
import org.apache.hc.core5.http.ParseException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotifyAudioSourceManager implements AudioSourceManager
{
    private static final String PATTERN = "^(?:http://|https://)?[a-z]+\\.spotify\\.com/([A-z]+)/([A-z0-9]+).*$";

    private static final Pattern SPOTIFY_REGEX = Pattern.compile(PATTERN);

    final YoutubeAudioSourceManager youtubeAudioSourceManager;

    public SpotifyAudioSourceManager(YoutubeAudioSourceManager youtubeAudioSourceManager)
    {
        this.youtubeAudioSourceManager = youtubeAudioSourceManager;
    }

    @Override
    public String getSourceName()
    {
        return "spotify";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager audioPlayerManager, AudioReference audioReference)
    {
        Matcher m = SPOTIFY_REGEX.matcher(audioReference.identifier);

        if(!m.matches())
            return null;

        try
        {
            return switch(m.group(1).toLowerCase())
            {
                case "track" -> getSpotifyTrack(m.group(2));
                case "album" -> getSpotifyAlbum(m.group(2));
                case "playlist" -> getSpotifyPlaylist(m.group(2));
                case "artist" -> getSpotifyArtist(m.group(2));
                default -> null;
            };
        }

        catch(Exception e)
        {
            throw new FriendlyException("Unable to load Spotify item!", FriendlyException.Severity.SUSPICIOUS, e);
        }
    }

    private AudioItem getSpotifyTrack(String id) throws IOException, ParseException, SpotifyWebApiException
    {
        GetTrackRequest getTrackRequest = Jukebox.getSpotifyApi().getTrack(id).build();
        Track track = getTrackRequest.execute();
        return buildTrack(track);
    }

    private AudioItem getSpotifyAlbum(String id) throws ExecutionException, InterruptedException
    {
        List<AudioTrack> playlist = new ArrayList<>();
        Future<Album> albumFuture = Jukebox.getSpotifyApi().getAlbum(id).build().executeAsync();
        Album album = albumFuture.get();

        for(TrackSimplified track : album.getTracks().getItems())
        {
            playlist.add(buildSimplifiedTrack(track));
        }

        return new BasicAudioPlaylist(album.getName(), playlist, null, false);
    }

    private AudioItem getSpotifyArtist(String id) throws IOException, ParseException, SpotifyWebApiException
    {
        List<AudioTrack> playlist = new ArrayList<>();
        GetArtistsTopTracksRequest getArtistsTopTracksRequest = Jukebox.getSpotifyApi().getArtistsTopTracks(id, CountryCode.US).build();
        Track[] tracks = getArtistsTopTracksRequest.execute();

        for(Track track: tracks)
        {
            playlist.add(buildTrack(track));
        }

        //TODO might need name
        return new BasicAudioPlaylist("", playlist, null, false);
    }

    private AudioItem getSpotifyPlaylist(String id)
    {
        try
        {
            int totalItems;
            int itemsProcessed;
            GetPlaylistsItemsRequest getPlaylistsItemsRequest = Jukebox.getSpotifyApi().getPlaylistsItems(id).build();
            Paging<PlaylistTrack> playlistTrackPaging = getPlaylistsItemsRequest.execute();
            List<AudioTrack> playlist = new ArrayList<>();

            totalItems = playlistTrackPaging.getTotal();
            itemsProcessed = 0;

            while(itemsProcessed < totalItems)
            {
                GetPlaylistsItemsRequest getMoreItemsRequest = Jukebox.getSpotifyApi().getPlaylistsItems(id).offset(itemsProcessed).build();
                Paging<PlaylistTrack> morePlayListTracks = getMoreItemsRequest.execute();
                List<PlaylistTrack> playlistTracks = List.of(morePlayListTracks.getItems());

                if (playlistTracks.isEmpty()) return null;

                for(PlaylistTrack playlistTrack: playlistTracks)
                {
                    itemsProcessed++;
                    if(Boolean.TRUE.equals(playlistTrack.getIsLocal())) continue;
                    IPlaylistItem item = playlistTrack.getTrack();
                    if (!(item instanceof Track track)) continue;

                    playlist.add(buildTrack(track));
                }
            }

            if (playlist.isEmpty())
                throw new FriendlyException("This playlist does not contain playable tracks (podcasts cannot be played)!", FriendlyException.Severity.COMMON, null);

            //TODO might need name
            return new BasicAudioPlaylist("", playlist, null, false);
        }

        catch (Exception e)
        {
            throw ExceptionTools.wrapUnfriendlyExceptions(e.getMessage(), FriendlyException.Severity.FAULT, e);
        }
    }

    private AudioTrack buildTrack(Track track)
    {
        AudioTrackInfo info = new AudioTrackInfo(
            track.getName(),
            track.getArtists()[0].getName(),
            track.getDurationMs(),
            track.getId(),
            false,
            track.getExternalUrls().get("spotify")
        );

        return new SpotifyAudioTrack(info, this);
    }

    private AudioTrack buildSimplifiedTrack(TrackSimplified track)
    {
        AudioTrackInfo info = new AudioTrackInfo(
            track.getName(),
            track.getArtists()[0].getName(),
            track.getDurationMs(),
            track.getId(),
            false,
            track.getExternalUrls().get("spotify")
        );

        return new SpotifyAudioTrack(info, this);
    }

    @Override
    public boolean isTrackEncodable(AudioTrack audioTrack) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack audioTrack, DataOutput dataOutput) {}

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo audioTrackInfo, DataInput dataInput)
    {
        return new SpotifyAudioTrack(audioTrackInfo,this);
    }

    @Override
    public void shutdown() {}
}
