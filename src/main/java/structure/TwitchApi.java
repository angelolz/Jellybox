package structure;

import com.google.gson.Gson;
import main.Jukebox;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TwitchApi
{
    public final String clientId;
    public final String clientSecret;
    public String accessToken;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    public TwitchApi(String clientId, String clientSecret)
    {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public void updateAccessToken()
    {
        try
        {
            Map<String, String> data = new HashMap<>();
            data.put("client_id", clientId);
            data.put("client_secret", clientSecret);
            data.put("grant_type", "client_credentials");

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(buildBodyWithData(data))
                    .uri(URI.create("https://id.twitch.tv/oauth2/token"))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            Gson gson = new Gson();
            TwitchAuthResponse t = gson.fromJson(response.body(), TwitchAuthResponse.class);
            this.accessToken = t.accessToken;
        }

        catch(Exception e)
        {
            Jukebox.getLogger().error("Unable to refresh Twitch token: {}", e.getMessage());
        }
    }

    public String getURL(String username) throws IOException, InterruptedException
    {
        HttpRequest request = HttpRequest.newBuilder().GET()
                .uri(URI.create("https://api.twitch.tv/helix/users?login=" + username))
                .setHeader("Client-Id", Jukebox.getTwitchApi().clientId)
                .setHeader("Authorization", "Bearer " + Jukebox.getTwitchApi().accessToken)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        Gson gson = new Gson();
        TwitchUserResponse t = gson.fromJson(response.body(), TwitchUserResponse.class);
        return t.getProfileUrl();
    }

    private HttpRequest.BodyPublisher buildBodyWithData(Map<String, String> data)
    {
        var builder = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet())
        {
            if (!builder.isEmpty())
                builder.append("&");

            builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }
}
