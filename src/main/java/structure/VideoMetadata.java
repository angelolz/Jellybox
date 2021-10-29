package structure;

import java.util.List;

public class VideoMetadata
{
    private String status;
    private Data data;
    private String message;

    public String getData()
    {
        return data.getTrack();
    }

    public String getStatus()
    {
        return status;
    }

    public String getMessage()
    {
        return message;
    }
}

class Data
{
    private String track;

    public String getTrack()
    {
        return track;
    }
}