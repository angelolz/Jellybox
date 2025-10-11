package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class Ping extends Command {

    public Ping() {
        this.name = "ping";
        this.help = "Returns the latency of the bot.";
        this.cooldown = 3;

        this.category = new Category("Bot");
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        MessageChannel channel = commandEvent.getChannel();
        String author = commandEvent.getAuthor().getAsMention();
        long time = System.currentTimeMillis();

        channel.sendMessage(":ping_pong: | " + author + ", Pong! ...")
               .queue(response -> response.editMessageFormat(":ping_pong: | " + author + ", Pong! %d ms",
                                              System.currentTimeMillis() - time)
                                          .queue());
    }
}
