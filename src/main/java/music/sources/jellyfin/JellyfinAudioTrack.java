package music.sources.jellyfin;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDetectionResult;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;

public class JellyfinAudioTrack extends DelegatedAudioTrack {

    private final JellyfinAudioSourceManager sourceManager;

    public JellyfinAudioTrack(AudioTrackInfo trackInfo, JellyfinAudioSourceManager sourceManager) {
        super(trackInfo);
        this.sourceManager = sourceManager;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        MediaContainerDetectionResult detection = sourceManager.detectContainer(new AudioReference(trackInfo.uri,
            trackInfo.title));
        MediaContainerDescriptor container = detection.getContainerDescriptor();
        HttpAudioTrack delegate = new HttpAudioTrack(trackInfo, container, sourceManager);
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
