package music.sources.jellyfin;

import com.google.gson.*;
import main.Config;
import structure.jellyfin.JellyfinTrack;
import utils.UtilClass;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JellyfinApi
{
    public static List<JellyfinTrack> searchTracks(String query) throws IOException
    {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = String.format("%s/Items?api_key=%s&parentId=%s&IncludeItemTypes=Audio&recursive=true&searchTerm=%s", Config.getJellyfinUrl(), Config.getJellyfinApiKey(), Config.getJellyfinLibraryId(), encoded);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("Accept", "application/json");

        try(InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))
        {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("Items");

            List<JellyfinTrack> results = new ArrayList<>();
            if(items == null) return results;

            for(JsonElement e : items)
            {
                JsonObject o = e.getAsJsonObject();
                JellyfinTrack track = extractMetadata(o);
                results.add(track);
            }
            return results;
        }
    }

    public static JellyfinTrack getAudioItemMetadata(String id) throws IOException
    {
        if(UtilClass.isNullOrEmpty(id)) return null;

        String url = String.format("%s/Items?api_key=%s&ids=%s&", Config.getJellyfinUrl(), Config.getJellyfinApiKey(), id);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("Accept", "application/json");

        try(InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))
        {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("Items");

            if(items == null) return null;
            JsonObject item = items.get(0).getAsJsonObject();
            return extractMetadata(item);
        }
    }

    private static JellyfinTrack extractMetadata(JsonObject item)
    {
        JellyfinTrack track = new JellyfinTrack();
        track.setId(item.get("Id").getAsString());
        track.setTrackName(item.get("Name").getAsString());
        track.setArtist(item.get("Artists").getAsJsonArray().get(0).getAsString());
        track.setLengthMs(item.get("RunTimeTicks").getAsLong() / 10_000);
        track.setContainer(item.get("Container").getAsString());
        track.setStreamUrl(JellyfinApi.getStreamUrl(item.get("Id").getAsString()));
        if(!UtilClass.isNullOrEmpty(item.get("Album").getAsString()))
            track.setAlbum(item.get("Album").getAsString());
        return track;
    }

    public static String getStreamUrl(String itemId)
    {
        return String.format("%s/Items/%s/File?api_key=%s",
            Config.getJellyfinUrl(),
            itemId,
            Config.getJellyfinApiKey()
        );
    }

    public static String getPlayableStreamUrl(JellyfinTrack track) {
        // Check if track is in a Lavaplayer-compatible format
        String container = track.getContainer().toLowerCase();

        if (container.equals("mp3") || container.equals("m4a")) {
            // Native playback
            return String.format("%s/Items/%s/file?api_key=%s",
                Config.getJellyfinUrl(),
                track.getId(),
                Config.getJellyfinApiKey());
        } else {
            // Unsupported → fallback to MP3 transcoding
            return String.format("%s/Items/%s/File?api_key=%s&userId=%s",
                Config.getJellyfinUrl(),
                track.getId(),
                Config.getJellyfinApiKey(),
                Config.getJellyfinUserId());
        }
    }
}
