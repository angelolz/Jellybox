package utils;

import java.net.URI;
import java.net.URISyntaxException;

public class URLUtils
{
    public static boolean isURI(String url)
    {
        try
        {
            new URI(url);
            return true;
        }

        catch(URISyntaxException e)
        {
            return false;
        }
    }
}
