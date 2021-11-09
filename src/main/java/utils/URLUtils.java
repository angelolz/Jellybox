package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class URLUtils
{
    public static boolean isURI(String urlString)
    {
        try
        {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        }

        catch(Exception e)
        {
            return false;
        }
    }

    public static String readURL(String string) throws IOException
    {
        URL url = new URL(string);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.addRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
        con.connect();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream())))
        {
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
            {
                buffer.append(chars, 0, read);
            }

            return buffer.toString();
        }
    }
}
