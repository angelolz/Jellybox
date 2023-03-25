package music.sources.spotify;

import com.google.gson.Gson;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import main.Jukebox;
import org.json.JSONArray;
import org.json.JSONObject;
import structure.YTSearch;
import utils.URLUtils;
import utils.YoutubeHostManager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SpotifyAudioTrack extends YoutubeAudioTrack
{
    private final SpotifyAudioSourceManager sourceManager;
    private String youtubeId;

    public SpotifyAudioTrack(AudioTrackInfo trackInfo, SpotifyAudioSourceManager sourceManager)
    {
        super(trackInfo, sourceManager.youtubeAudioSourceManager);
        this.sourceManager = sourceManager;
    }

    @Override
    public String getIdentifier()
    {
        if(youtubeId == null)
        {
            try
            {
                AudioTrackInfo info = this.trackInfo;
                YoutubeHostManager ytHostManager = new YoutubeHostManager();
                Gson gson = new Gson();
                String json = null;
                while(json == null && !ytHostManager.noHostAvailable()) {
                    try {
                        json = URLUtils.readURL(String.format("%sapi/v1/search?pretty=1&q=%s%%20%s",
                                ytHostManager.getNext(),
                                URLEncoder.encode(info.author, StandardCharsets.UTF_8),
                                URLEncoder.encode(info.title, StandardCharsets.UTF_8))
                            .replaceAll("\\+", "%20"));
                    }

                    catch(IOException e) {
                        Jukebox.getLogger().debug("Failed to get json: {}", e.getMessage());
                    }
                }

                if(json != null) {
                    JSONArray jsonArray = new JSONArray(json);
                    JSONObject jsonObj = jsonArray.getJSONObject(0);
                    YTSearch y = gson.fromJson(jsonObj.toString(), YTSearch.class);

                    youtubeId = y.videoId;
                    this.setIdentifier(y.videoId);
                }

                else
                    throw new Exception(String.format("Couldn't find host for: %s - %s", info.author, info.title));
            }

            catch(Exception e)
            {
                Jukebox.getLogger().error("Error when getting spotify identifier: {}", e.getMessage());
            }
        }

        return youtubeId;
    }

    @Override
    public AudioSourceManager getSourceManager()
    {
        return this.sourceManager;
    }

    private void setIdentifier(String videoId)
    {
        final Class<AudioTrackInfo> infoCls = AudioTrackInfo.class;
        try
        {
            final Field identifier = infoCls.getDeclaredField("identifier");
            final Field uri = infoCls.getDeclaredField("uri");


            identifier.setAccessible(true);
            identifier.set(this.trackInfo, videoId);
            uri.setAccessible(true);
            uri.set(this.trackInfo, "https://youtu.be/" + videoId);
        }

        catch (NoSuchFieldException | IllegalAccessException e)
        {
            throw new FriendlyException("Failed to look up YouTube track!", FriendlyException.Severity.SUSPICIOUS, e);
        }
    }

    @Override
    protected AudioTrack makeShallowClone()
    {
        return new SpotifyAudioTrack(trackInfo, sourceManager);
    }
}
