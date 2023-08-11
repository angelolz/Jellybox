package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Admin extends Command
{
    public Admin()
    {
        this.name = "admin";
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent commandEvent)
    {
        String[] args = commandEvent.getArgs().split("\\s+");

        switch(args[0].toLowerCase())
        {
            case "servers" -> {
                StringBuilder sb = new StringBuilder();
                for(Guild guild : commandEvent.getJDA().getGuilds())
                {
                    sb.append(guild.getName()).append(":").append(guild.getId()).append("\n");
                }

                commandEvent.reply(sb.toString());
            }

            case "leave" -> {
                Stream<Guild> guildStream = commandEvent.getJDA().getGuilds().stream().filter(guild -> guild.getId().equals(args[1]));
                List<Guild> guildLeftList = new ArrayList<>();

                guildStream.forEach(guild -> guild.leave().queue(s -> guildLeftList.add(guild)));

                if(guildLeftList.isEmpty()) {
                    commandEvent.reply(":x: | no servers found with that id.");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                for(Guild guild : guildLeftList) {
                    sb.append(String.format("✅ left server: %s - %s", guild.getName(), guild.getId()));
                }

                commandEvent.reply(sb.toString());
            }

            default -> commandEvent.reply("what do u want loser");
        }
    }
}
