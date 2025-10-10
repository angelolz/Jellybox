package music.sources.jellyfin;

import com.sedmelluq.discord.lavaplayer.container.*;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.PersistentHttpStream;
import com.sedmelluq.discord.lavaplayer.track.*;

import structure.jellyfin.JellyfinTrack;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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
            // Extract Jellyfin item ID
            String id = reference.identifier.replace("jellyfin://", "");

            // Fetch metadata and stream URL
            JellyfinTrack track = JellyfinApi.getAudioItemMetadata(id);
            String streamUrl = JellyfinApi.getStreamUrl(id);

            // Build track info using Jellyfin metadata
            AudioTrackInfo info = new AudioTrackInfo(
                track.getTrackName(),
                track.getArtist(),
                track.getLengthMs(),
                streamUrl,
                false,
                streamUrl
            );

            // Create and return a JellyfinAudioTrack
            return new JellyfinAudioTrack(info, this);

        } catch (Exception e) {
            throw new FriendlyException(
                "Failed to load Jellyfin track",
                FriendlyException.Severity.SUSPICIOUS,
                e
            );
        }
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return false;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        // You can skip encoding if you don't need to store tracks in memory
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return new JellyfinAudioTrack(trackInfo, this);
    }

    @Override
    public void shutdown() {
        // Nothing to clean up
    }

    public MediaContainerDetectionResult detectContainer(AudioReference reference) {
        try (HttpInterface httpInterface = this.getHttpInterface()) {
            return this.detectContainerWithClient(httpInterface, reference);
        } catch (IOException e) {
            throw new FriendlyException("Connecting to the URL failed.", FriendlyException.Severity.SUSPICIOUS, e);
        }
    }

    public MediaContainerDetectionResult detectContainerWithClient(HttpInterface httpInterface, AudioReference reference) throws IOException {
        try (PersistentHttpStream inputStream = new PersistentHttpStream(httpInterface, new URI(reference.identifier), Long.MAX_VALUE)) {
            int statusCode = inputStream.checkStatusCode();
            String redirectUrl = HttpClientTools.getRedirectLocation(reference.identifier, inputStream.getCurrentResponse());
            if (redirectUrl != null) {
                return MediaContainerDetectionResult.refer((MediaContainerProbe)null, new AudioReference(redirectUrl, (String)null));
            } else if (statusCode == 404) {
                return null;
            } else if (!HttpClientTools.isSuccessWithContent(statusCode)) {
                throw new FriendlyException("That URL is not playable.", FriendlyException.Severity.COMMON, new IllegalStateException("Status code " + statusCode));
            } else {
                MediaContainerHints hints = MediaContainerHints.from(HttpClientTools.getHeaderValue(inputStream.getCurrentResponse(), "Content-Type"), (String)null);
                return (new MediaContainerDetection(this.containerRegistry, reference, inputStream, hints)).detectContainer();
            }
        } catch (URISyntaxException e) {
            throw new FriendlyException("Not a valid URL.", FriendlyException.Severity.COMMON, e);
        }
    }
}