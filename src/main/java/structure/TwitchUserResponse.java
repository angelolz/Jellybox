package structure;

import com.google.gson.annotations.SerializedName;

public class TwitchUserResponse
{
    public static class UserData
    {
        @SerializedName("profile_image_url")
        private String profileImageUrl;
    }

    private UserData[] data;

    public String getProfileUrl()
    {
        return data[0].profileImageUrl;
    }
}
