package structure.jellyfin;

import lombok.Data;

@Data
public class JellyfinAlbum {
    private String id;
    private String albumArtist;
    private String albumName;
    private long lengthMs;
    private long year;
}
