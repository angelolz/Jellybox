package listeners;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import main.Jukebox;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ScheduledTasks extends ListenerAdapter
{
    private final CommandClient client;

    public ScheduledTasks(CommandClient client)
    {
        this.client = client;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event)
    {
        //get refresh tokens for spotify and twitch api
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(ScheduledTasks::refreshToken, 0, 1, TimeUnit.HOURS);

        //cache help commands
        cacheCommands(client);
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

    private void cacheCommands(CommandClient client)
    {
        Hashtable<String, List<Command>> helpCache = Jukebox.getHelpCache();

        if(!helpCache.containsKey("player") || !helpCache.containsKey("bot") || !helpCache.containsKey("tool"))
        {
            ArrayList<Command> playerCommands = new ArrayList<>();
            ArrayList<Command> botCommands = new ArrayList<>();
            ArrayList<Command> toolCommands = new ArrayList<>();

            for(Command command: client.getCommands())
            {
                if(!command.isHidden() && !command.isOwnerCommand())
                {
                    switch(command.getCategory().getName().toLowerCase(Locale.ROOT))
                    {
                        case "bot" -> botCommands.add(command);
                        case "player" -> playerCommands.add(command);
                        case "tools" -> toolCommands.add(command);
                    }
                }
            }

            helpCache.put("bot", botCommands);
            helpCache.put("player", playerCommands);
            helpCache.put("tool", toolCommands);
        }
    }
}
