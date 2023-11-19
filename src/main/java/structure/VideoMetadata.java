package structure;

import lombok.Getter;

@Getter
public class VideoMetadata
{
    @Getter
    private static class Data
    {
        private String track;
    }

    private String status;
    private Data data;
    private String message;
}