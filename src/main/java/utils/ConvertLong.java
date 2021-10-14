package utils;

public class ConvertLong
{
    public static String convertLongToTrackTime(long length)
    {
        int hours = (int) (length / (1000*60*60));
        int minutes = (int) ((length / (1000*60)) % 60);
        int seconds = (int) ((length / 1000) % 60);

        if(hours > 0)
            return String.format("%d:%d:%d", hours, minutes, seconds);
        else
            return String.format("%d:%d", minutes, seconds);
    }

    public static String convertLongToDaysLength(long ms)
    {
        int days, hours, minutes, seconds;
        StringBuilder result = new StringBuilder();

        seconds = (int) ((ms / 1000) % 60);
        minutes = (int) ((ms / (1000*60)) % 60);
        hours = (int) ((ms / (1000*60*60)) % 24);
        days = (int) ((ms / (1000*60*60*24)));

        if(days > 0)
        {
            result.append(String.format("%d days, %d hours, %d minutes, and %d seconds",
                days, hours, minutes, seconds));
        }

        else if(hours > 0)
        {
            result.append(String.format("%d hours, %d minutes, and %d seconds",
                hours, minutes, seconds));
        }

        else if(minutes > 0)
        {
            result.append(String.format("%d minutes, and %d seconds",
                minutes, seconds));
        }

        else
            result.append(String.format("%d seconds", seconds));

        return result.toString();
    }
}
