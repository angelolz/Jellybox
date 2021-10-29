package commands;

import com.google.gson.Gson;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import core.GLA;
import music.GuildMusicManager;
import music.PlayerManager;
import net.dv8tion.jda.api.entities.MessageChannel;
import structure.VideoMetadata;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Lyrics extends Command
{

    private GLA lyricsGetter;

    public Lyrics()
    {
        this.name = "lyrics";
        this.help = "Returns lyrics for any song";
        this.cooldown = 3;
        this.arguments = "!lyrics <song-name> - Returns <song-name>'s lyrics";

        // Set up for Genius Lyrics API
        try
        {
            // Set up for GLA
            Properties prop = new Properties();
            FileInputStream propFile = new FileInputStream("config.properties");
            prop.load(propFile);
            lyricsGetter = new GLA(prop.getProperty("gla_id"), prop.getProperty("gla_access_token"));
        }
        catch (IOException e)
        {
            System.out.println("Config file error!");
        }
    }
    @Override
    protected void execute(CommandEvent event)
    {
        try{
            GuildMusicManager manager = PlayerManager.getInstance().getMusicManager(event.getGuild());
            AudioPlayer player = manager.getScheduler().getPlayer();
            AudioTrack track = player.getPlayingTrack();

            MessageChannel channel = event.getChannel();
            String search = event.getArgs();
            if(search.equals("")){
                search = currentSongQuery(track);
            }
            String[] lyrics = lyricsGetter.search(search).get(0).getText().split("\n");
            formatLyrics(channel, lyrics);
        }
        catch (IndexOutOfBoundsException e)
        {
            MessageChannel channel = event.getChannel();
            channel.sendMessage("Could not find the song!").queue();
        }
        catch (NullPointerException e)
        {
            MessageChannel channel = event.getChannel();
            channel.sendMessage("There is no song currently playing!\nUse !lyrics <song-name> to search for a specific song.").queue();
        }
        catch (RuntimeException e)
        {
            MessageChannel channel = event.getChannel();
            channel.sendMessage(e.getMessage()).queue();
        }
        catch(IOException e)
        {
            MessageChannel channel = event.getChannel();
            channel.sendMessage("An error has occurred! Please try again later.").queue();
        }
    }

    private String currentSongQuery(AudioTrack track) throws IOException
    {

        String parsedQuery = URLEncoder.encode(track.getInfo().title, StandardCharsets.UTF_8).replaceAll("%23", "#");
        String fullURL = "https://metadata-filter.vercel.app/api/youtube?track=" + parsedQuery;

        URL jsonURL = new URL(fullURL);
        InputStreamReader reader = new InputStreamReader(jsonURL.openStream());

        Gson metadata = new Gson();

        VideoMetadata query = metadata.fromJson(reader, VideoMetadata.class);

        if(!query.getStatus().equals("success"))
        {
            throw new RuntimeException(query.getMessage());
        }

        return query.getData();
    }

    private void formatLyrics(MessageChannel channel, String[] lyrics)
    {
        StringBuilder sb = new StringBuilder();
        for(String line: lyrics)
        {
            if((sb.length() + line.length()) > 1900)
            {
                channel.sendMessage(sb.toString()).queue();
                sb = new StringBuilder();
            }
            else
            {
                sb.append(line).append("\n");
            }
        }

        if(!sb.toString().equals(""))
        {
            channel.sendMessage(sb).queue();
        }
    }
}
