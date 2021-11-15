package structure;

public class VideoMetadata
{
    static class Data
    {
        private String track;

        public String getTrack()
        {
            return track;
        }
    }

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