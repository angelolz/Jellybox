package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class Lyrics extends Command
{

    public Lyrics()
    {
        this.name = "lyrics";
        this.help = "Returns lyrics for any song";
        this.cooldown = 3;
        this.arguments = "!lyrics <song-name> - Returns <song-name>'s lyrics";
    }
    @Override
    protected void execute(CommandEvent event)
    {

    }


}
