package music.sources.jellyfin;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDetectionResult;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;

import java.net.URI;

public class JellyfinAudioTrack extends DelegatedAudioTrack {
    private final JellyfinAudioSourceManager sourceManager;

    public JellyfinAudioTrack(AudioTrackInfo trackInfo, JellyfinAudioSourceManager sourceManager) {
        super(trackInfo);
        this.sourceManager = sourceManager;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        // Detect the container using your source manager
        MediaContainerDetectionResult detection = sourceManager.detectContainer(new AudioReference(trackInfo.uri, trackInfo.title));

        if (detection == null || detection.getContainerDescriptor() == null) {
            throw new RuntimeException("Could not detect container for Jellyfin track: " + trackInfo.uri);
        }

        MediaContainerDescriptor container = detection.getContainerDescriptor();

        // Create a HttpAudioTrack using the detected container
        HttpAudioTrack delegate = new HttpAudioTrack(trackInfo, container, sourceManager);

        // Delegate actual playback
        processDelegate(delegate, executor);
    }

    @Override
    public AudioTrack makeClone() {
        return new JellyfinAudioTrack(trackInfo, sourceManager);
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return sourceManager;
    }
}
