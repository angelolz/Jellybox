package utils;

public class ConvertLong
{
    public static String convertLongToTrackTime(long length)
    {
        int hours = (int) (length / (1000*60*60));
        int minutes = (int) ((length / (1000*60)) % 60);
        int seconds = (int) ((length / 1000) % 60);

        if(hours > 0)
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        else
            return String.format("%02d:%02d", minutes, seconds);
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
            result.append(String.format("%d days, %02d:%02d:%02d", days, hours, minutes, seconds));

        else
            result.append(String.format("%02d:%02d:%02d", hours, minutes, seconds));

        return result.toString();
    }
}
