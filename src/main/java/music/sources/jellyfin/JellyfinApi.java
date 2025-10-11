package music.sources.jellyfin;

import com.google.gson.*;
import main.Config;
import structure.jellyfin.JellyfinAlbum;
import structure.jellyfin.JellyfinArtist;
import structure.jellyfin.JellyfinTrack;
import utils.UtilClass;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JellyfinApi {

    public static List<JellyfinTrack> searchTracks(String query) throws IOException {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url =
            String.format("%s/Items?api_key=%s&parentId=%s&IncludeItemTypes=Audio&recursive=true&limit=10&searchTerm" + "=%s",
                Config.getJellyfinUrl(), Config.getJellyfinApiKey(), Config.getJellyfinLibraryId(), encoded);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("Accept", "application/json");

        try(InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("Items");

            List<JellyfinTrack> results = new ArrayList<>();
            if(items == null)
                return results;

            for(JsonElement e : items) {
                JsonObject o = e.getAsJsonObject();
                JellyfinTrack track = extractTrackMetadata(o);
                results.add(track);
            }
            return results;
        }
    }

    public static List<JellyfinAlbum> searchAlbums(String query) throws IOException {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = String.format("%s/Items?api_key=%s&parentId=%s&IncludeItemTypes=MusicAlbum&limit=10&recursive=true" +
            "&searchTerm=%s", Config.getJellyfinUrl(), Config.getJellyfinApiKey(), Config.getJellyfinLibraryId()
            , encoded);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("Accept", "application/json");

        try(InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("Items");

            List<JellyfinAlbum> results = new ArrayList<>();
            if(items == null)
                return results;

            for(JsonElement e : items) {
                JsonObject o = e.getAsJsonObject();
                JellyfinAlbum album = extractAlbumMetadata(o);
                results.add(album);
            }
            return results;
        }
    }

    public static List<JellyfinArtist> searchArtists(String query) throws IOException {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = String.format("%s/Artists/AlbumArtists?api_key=%s&parentId=%s&limit=10&SearchTerm=%s", Config.getJellyfinUrl(), Config.getJellyfinApiKey(), Config.getJellyfinLibraryId(), encoded);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("Accept", "application/json");

        try(InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("Items");

            List<JellyfinArtist> results = new ArrayList<>();
            if(items == null)
                return results;

            for(JsonElement e : items) {
                JsonObject o = e.getAsJsonObject();
                JellyfinArtist track = extractArtistMetadata(o);
                results.add(track);
            }
            return results;
        }
    }

    public static List<JellyfinTrack> getAlbumTracks(String albumId) throws IOException {
        String url = String.format("%s/Items?api_key=%s&parentId=%s&IncludeItemTypes=Audio&recursive=true&SortBy" +
            "=IndexNumber", Config.getJellyfinUrl(), Config.getJellyfinApiKey(), albumId);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("Accept", "application/json");

        try(InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("Items");

            List<JellyfinTrack> results = new ArrayList<>();
            if(items == null)
                return results;

            for(JsonElement e : items) {
                JsonObject o = e.getAsJsonObject();
                JellyfinTrack track = extractTrackMetadata(o);
                results.add(track);
            }
            return results;
        }
    }

    public static List<JellyfinTrack> getArtistTracks(String artistId) throws IOException {
        String url = String.format("%s/Items?api_key=%s&parentId=%s&IncludeItemTypes=Audio&recursive=true&SortBy=IndexNumber&SortBy=SortName&SortOrder=Ascending&ArtistIds=%s", Config.getJellyfinUrl(), Config.getJellyfinApiKey(), Config.getJellyfinLibraryId(), artistId);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("Accept", "application/json");

        try(InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("Items");

            List<JellyfinTrack> results = new ArrayList<>();
            if(items == null)
                return results;

            for(JsonElement e : items) {
                JsonObject o = e.getAsJsonObject();
                JellyfinTrack track = extractTrackMetadata(o);
                results.add(track);
            }
            return results;
        }
    }

    public static JellyfinTrack getTrackMetadata(String trackId) throws IOException {
        if(UtilClass.isNullOrEmpty(trackId))
            return null;

        String url = String.format("%s/Items?api_key=%s&ids=%s&", Config.getJellyfinUrl(), Config.getJellyfinApiKey()
            , trackId);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("Accept", "application/json");

        try(InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("Items");

            if(items == null)
                return null;
            JsonObject item = items.get(0).getAsJsonObject();
            return extractTrackMetadata(item);
        }
    }

    public static JellyfinAlbum getAlbumMetadata(String albumId) throws IOException {
        if(UtilClass.isNullOrEmpty(albumId))
            return null;

        String url = String.format("%s/Items?api_key=%s&ids=%s&", Config.getJellyfinUrl(), Config.getJellyfinApiKey()
            , albumId);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("Accept", "application/json");

        try(InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("Items");

            if(items == null)
                return null;
            JsonObject item = items.get(0).getAsJsonObject();
            return extractAlbumMetadata(item);
        }
    }

    public static JellyfinArtist getArtistMetadata(String artistId) throws IOException {
        if(UtilClass.isNullOrEmpty(artistId))
            return null;

        String url = String.format("%s/Items?api_key=%s&ids=%s&", Config.getJellyfinUrl(), Config.getJellyfinApiKey(), artistId);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("Accept", "application/json");

        try(InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("Items");

            if(items == null)
                return null;
            JsonObject item = items.get(0).getAsJsonObject();
            return extractArtistMetadata(item);
        }
    }

    public static JellyfinTrack extractTrackMetadata(JsonObject item) {
        JellyfinTrack track = new JellyfinTrack();
        track.setId(item.get("Id").getAsString());
        track.setTrackName(item.get("Name").getAsString());
        track.setArtist(item.get("Artists").getAsJsonArray().get(0).getAsString());
        track.setLengthMs(item.get("RunTimeTicks").getAsLong() / 10_000);
        track.setContainer(item.get("Container").getAsString());
        track.setStreamUrl(JellyfinApi.getStreamUrl(item.get("Id").getAsString()));
        if(!UtilClass.isNullOrEmpty(item.get("Album").getAsString()))
            track.setAlbum(item.get("Album").getAsString());
        if(!UtilClass.isNullOrEmpty(item.get("AlbumId").getAsString()))
            track.setAlbumId(item.get("AlbumId").getAsString());
        return track;
    }

    public static JellyfinAlbum extractAlbumMetadata(JsonObject item) {
        JellyfinAlbum album = new JellyfinAlbum();
        album.setId(item.get("Id").getAsString());
        album.setAlbumName(item.get("Name").getAsString());
        album.setAlbumArtist(item.get("AlbumArtist").getAsString());
        album.setLengthMs(item.get("RunTimeTicks").getAsLong() / 10_000);
        album.setYear(item.get("ProductionYear").getAsLong());
        return album;
    }

    public static JellyfinArtist extractArtistMetadata(JsonObject item) {
        JellyfinArtist artist = new JellyfinArtist();
        artist.setId(item.get("Id").getAsString());
        artist.setName(item.get("Name").getAsString());
        return artist;
    }

    public static String getStreamUrl(String itemId) {
        return String.format("%s/Items/%s/File?api_key=%s", Config.getJellyfinUrl(), itemId,
            Config.getJellyfinApiKey());
    }

    public static String getAlbumThumbnail(String itemId) {
        return String.format("%s/Items/%s/Images/Primary?api_key%s", Config.getJellyfinUrl(), itemId,
            Config.getJellyfinApiKey());
    }
}
