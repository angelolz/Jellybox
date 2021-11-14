package utils;

import main.Jukebox;

import java.util.ArrayList;

public class LyricsFetcher
{
    public static ArrayList<String> get(String key)
    {
        String[] lyrics = Jukebox.getGeniusApi().search(key).get(0).getText().split("\n");
        ArrayList<String> formattedLyrics = new ArrayList<>();
        StringBuilder lyricsChunk = new StringBuilder();

        for(String line: lyrics)
        {
            if((lyricsChunk.length() + line.length()) > 3900 || line.isEmpty()) //Trigger when the line length is too long or there is a break in the lyrics
            {
                if(lyricsChunk.length() > 0)
                {
                    formattedLyrics.add(lyricsChunk.toString());
                }
                lyricsChunk = new StringBuilder(); // Create a new string builder to handle the new block
                if(!line.isEmpty()) lyricsChunk.append(line).append("\n");
            }

            else
            {
                if(line.charAt(0) == '[' && line.charAt(line.length() - 1) == ']') // Check if the current line is a title of the block
                {
                    if(lyricsChunk.length() > 0)
                    {
                        formattedLyrics.add(lyricsChunk.toString());
                    }

                    lyricsChunk = new StringBuilder(); // Create a new string builder to handle the
                    lyricsChunk.append("**").append(line).append("**").append("\n");
                }

                else
                {
                    lyricsChunk.append(line).append("\n");
                }
            }
        }

        if(!lyricsChunk.isEmpty())
        {
            formattedLyrics.add(lyricsChunk.toString());
        }
        return formattedLyrics;
    }
}
