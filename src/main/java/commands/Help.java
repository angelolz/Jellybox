package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import main.Jukebox;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Arrays;
import java.util.List;

public class Help extends Command
{
    public Help()
    {
        this.name = "help";
        this.help = "Shows you a list of the bot's commands.";
        this.cooldown = 3;
    }

    @Override
    protected void execute(CommandEvent commandEvent)
    {
        EmbedBuilder embed = new EmbedBuilder();

        if(!commandEvent.getArgs().isEmpty())
        {
            // goes through all command to find a match
            for(Command command : commandEvent.getClient().getCommands())
            {
                if(command.getName().equalsIgnoreCase(commandEvent.getArgs()))
                {
                    embed.setTitle("Command Info");
                    embed.setColor(0x32CD32);
                    embed.setDescription(command.getHelp());
                    embed.addField("Arguments", command.getArguments() == null ? "*None*" : command.getArguments(), true);
                    embed.addField("Cooldown Time", String.valueOf(command.getCooldown()), true);

                    //convert aliases to list object so i can print it out nicely
                    List<String> list = Arrays.asList(getAliases());
                    String stringList = String.join(", ", list);
                    embed.addField("Aliases", stringList.isEmpty() ? "*None*" : stringList, true);

                    commandEvent.reply(embed.build());
                    return;
                }
            }

            // if unable to find a match, show error message
            commandEvent.reply(":x: | Sorry, there's no command that matches that name!");
        }

        else
        {
            embed.setTitle("Jukebox Commands");
            embed.setColor(0x32CD32);
            embed.setDescription("Here are a list of commands you can use!");
            embed.setFooter("Version " + Jukebox.getVersion());
            embed.setThumbnail(commandEvent.getJDA().getSelfUser().getAvatarUrl());

            for(Command command : commandEvent.getClient().getCommands())
            {
                if(!command.isHidden() && !command.isOwnerCommand())
                {
                    String commandName = String.format("%s%s", Jukebox.getPrefix(), command.getName());
                    if(command.getAliases().length > 0)
                    {
                        String[] aliases = command.getAliases();
                        for (String alias : aliases)
                        {
                            commandName = commandName.concat("/" + alias);
                        }
                    }

                    embed.addField(commandName, command.getHelp(), true);
                }
            }

            commandEvent.reply(embed.build());
        }
    }
}