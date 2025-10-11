package structure.jellyfin;

import lombok.Data;

@Data
public class JellyfinTrack {

    private String id;
    private String albumId;
    private String trackName;
    private String artist;
    private String album;
    private long lengthMs;
    private String container;
    private String streamUrl;
}
