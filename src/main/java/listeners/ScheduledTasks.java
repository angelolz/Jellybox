package listeners;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import main.Jukebox;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ScheduledTasks
{
    public static void init()
    {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(ScheduledTasks::refreshToken, 0, 1, TimeUnit.HOURS);
    }

    private static void refreshToken()
    {
        SpotifyApi api = Jukebox.getSpotifyApi();
        ClientCredentialsRequest clientCredentialsRequest = api.clientCredentials().build();
        try
        {
            final ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            api.setAccessToken(clientCredentials.getAccessToken());
            Jukebox.getLogger().debug("Got a new access token from Spotify.");
        }

        catch(Exception e)
        {
            Jukebox.getLogger().error("Failed to get new access token: {}", e.toString());
        }
    }
}
