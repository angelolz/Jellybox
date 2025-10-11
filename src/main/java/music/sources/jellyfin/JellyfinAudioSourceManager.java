package music.sources.jellyfin;

import com.sedmelluq.discord.lavaplayer.container.*;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.PersistentHttpStream;
import com.sedmelluq.discord.lavaplayer.track.*;

import main.Jellybox;
import structure.jellyfin.JellyfinAlbum;
import structure.jellyfin.JellyfinArtist;
import structure.jellyfin.JellyfinTrack;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom source manager that loads audio items from a Jellyfin server.
 */
public class JellyfinAudioSourceManager extends HttpAudioSourceManager {

    @Override
    public String getSourceName() {
        return "jellyfin";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        try {
            String removedHandler = reference.identifier.replace("jellyfin://", "");
            String[] params = removedHandler.split("/");
            switch(params[0]) {
                case "track" -> { return loadTrack(params[1]); }
                case "album" -> { return loadAlbum(params[1]); }
                case "artist" -> { return loadArtist(params[1]); }
                default -> Jellybox.getLogger().error("Unknown handler: {} | type: {}", params[1], reference.identifier);
            }
        }
        catch(Exception e) {
            throw new FriendlyException("Failed to load item", FriendlyException.Severity.SUSPICIOUS, e);
        }

        return null;
    }

    private JellyfinAudioTrack createAudioTrack(JellyfinTrack track) {
        String streamUrl = JellyfinApi.getStreamUrl(track.getId());
        String artworkUrl = JellyfinApi.getAlbumThumbnail(track.getAlbumId() != null ? track.getAlbumId() :
            track.getId());

        //the isrc field is used to show album name
        AudioTrackInfo info = new AudioTrackInfo(track.getTrackName(), track.getArtist(), track.getLengthMs(),
            streamUrl, false, streamUrl, artworkUrl, track.getAlbum());

        return new JellyfinAudioTrack(info, this);
    }

    private AudioItem loadTrack(String id) throws IOException {
        JellyfinTrack track = JellyfinApi.getTrackMetadata(id);
        return createAudioTrack(track);
    }

    private AudioPlaylist loadAlbum(String id) throws IOException {
        List<JellyfinTrack> tracks = JellyfinApi.getAlbumTracks(id);
        JellyfinAlbum album = JellyfinApi.getAlbumMetadata(id);
        List<AudioTrack> audioTracks = tracks.stream().map(this::createAudioTrack).collect(Collectors.toList());

        return new BasicAudioPlaylist(String.format("%s - %s (%s)", album.getAlbumArtist(), album.getAlbumName(),
            album.getYear()), audioTracks, null, false);
    }

    private AudioPlaylist loadArtist(String id) throws IOException {
        List<JellyfinTrack> tracks = JellyfinApi.getArtistTracks(id);
        JellyfinArtist artist = JellyfinApi.getArtistMetadata(id);
        List<AudioTrack> audioTracks = tracks.stream().map(this::createAudioTrack).collect(Collectors.toList());

        return new BasicAudioPlaylist(artist.getName(), audioTracks, null, false);
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) { return false; }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) { }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return new JellyfinAudioTrack(trackInfo, this);
    }

    @Override
    public void shutdown() { }

    public MediaContainerDetectionResult detectContainer(AudioReference reference) {
        try(HttpInterface httpInterface = this.getHttpInterface()) {
            return this.detectContainerWithClient(httpInterface, reference);
        }
        catch(IOException e) {
            throw new FriendlyException("Connecting to the URL failed.", FriendlyException.Severity.SUSPICIOUS, e);
        }
    }

    public MediaContainerDetectionResult detectContainerWithClient(HttpInterface httpInterface,
                                                                   AudioReference reference) throws IOException {
        try(PersistentHttpStream inputStream = new PersistentHttpStream(httpInterface, new URI(reference.identifier),
            Long.MAX_VALUE)) {
            int statusCode = inputStream.checkStatusCode();
            String redirectUrl = HttpClientTools.getRedirectLocation(reference.identifier,
                inputStream.getCurrentResponse());
            if(redirectUrl != null) {
                return MediaContainerDetectionResult.refer((MediaContainerProbe) null, new AudioReference(redirectUrl
                    , (String) null));
            }
            else if(statusCode == 404) {
                return null;
            }
            else if(!HttpClientTools.isSuccessWithContent(statusCode)) {
                throw new FriendlyException("That URL is not playable.", FriendlyException.Severity.COMMON,
                    new IllegalStateException("Status code " + statusCode));
            }
            else {
                MediaContainerHints hints =
                    MediaContainerHints.from(HttpClientTools.getHeaderValue(inputStream.getCurrentResponse(),
                        "Content-Type"), (String) null);
                return (new MediaContainerDetection(this.containerRegistry, reference, inputStream, hints)).detectContainer();
            }
        }
        catch(URISyntaxException e) {
            throw new FriendlyException("Not a valid URL.", FriendlyException.Severity.COMMON, e);
        }
    }
}