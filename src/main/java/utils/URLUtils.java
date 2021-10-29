package utils;

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
}
