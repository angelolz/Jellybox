package structure;

public class TwitchUserResponse
{
    public static class UserData
    {
        public String profile_image_url;
    }

    public UserData[] data;

    public String getProfileUrl()
    {
        return data[0].profile_image_url;
    }
}
