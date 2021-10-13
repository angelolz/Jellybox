package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import structure.*;


public class Add extends Command{

    private MusicQueue mQueue;
    private Song song;

    public Add(String url)
    {
        song = new Song(url);
        this.name = "add";
        this.help = "adds specified song to queue";
        this.cooldown = 3;
    }

    @Override
    protected void execute(CommandEvent commandEvent)
    {
        mQueue.add(song);
    }
}

