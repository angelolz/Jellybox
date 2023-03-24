package utils;

public class YoutubeHostManager
{
    private final static String[] hosts = new String[] {
        "https://vid.puffyan.us/",
        "https://inv.riverside.rocks/",
        "https://invidious.slipfox.xyz/",
        "https://invidious.snopyta.org/",
    };

    private int index;
    private boolean noHostAvailable;

    public YoutubeHostManager()
    {
        index = 0;
        noHostAvailable = false;
    }

    public String getNext()
    {
        if(index < hosts.length)
        {
            String host = hosts[index];
            index++;

            if (index == hosts.length) noHostAvailable = true;
            return host;
        }

        else {
            return "";
        }
    }

    public boolean noHostAvailable() {
        return noHostAvailable;
    }
}
