package listeners;

import main.Jukebox;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ScheduledTasks extends ListenerAdapter
{
    @Override
    public void onReady(@NotNull ReadyEvent event)
    {
        //get refresh tokens for spotify and twitch api
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(ScheduledTasks::refreshToken, 0, 1, TimeUnit.HOURS);

        event.getJDA().openPrivateChannelById(Jukebox.getOwnerId()).queue(
            dm -> dm.sendMessageFormat("Don't forget to get po_token and visitor data using %sadmin token <poToken> <vistorData>", Jukebox.getPrefix()).queue()
        );
    }

    private static void refreshToken()
    {
        //refresh spotify token
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

        //refresh twitch token
        Jukebox.getTwitchApi().updateAccessToken();
    }
}
