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
        this.arguments = "[cmd name]";
        this.help = "Shows you a list of the available commands.";
        this.cooldown = 3;
    }

    @Override
    protected void execute(CommandEvent commandEvent)
    {
        EmbedBuilder embed = new EmbedBuilder().setColor(0x409df5);

        if(!commandEvent.getArgs().isEmpty())
        {
            // goes through all command to find a match
            for(Command command : commandEvent.getClient().getCommands())
            {
                String[] aliases = command.getAliases();

                if(command.getName().equalsIgnoreCase(commandEvent.getArgs()) ||
                        (aliases != null && Arrays.stream(aliases).anyMatch(a -> a.equalsIgnoreCase(commandEvent.getArgs()))))
                {
                    embed.setTitle(String.format("Command Info for `%s%s`", Jukebox.getPrefix(), command.getName()));
                    embed.addField("Arguments", command.getArguments() == null ? "*None*" : command.getArguments(), true);
                    embed.addField("Cooldown Time", String.valueOf(command.getCooldown()), true);

                    //convert aliases to list object so it can print it out nicely
                    List<String> list = Arrays.asList(command.getAliases());
                    String stringList = String.join(", ", list);
                    embed.addField("Aliases", stringList.isEmpty() ? "*None*" : stringList, true);

                    switch(command.getName())
                    {
                        case "help":
                            embed.setDescription("Shows an embed listing all of the available commands as well as detailed help for each of them. " +
                                    "If you provide a command name or its alias as an argument, it would show the usage of the command in more detail.");
                            break;
                        case "join":
                            embed.setDescription("Joins the same voice channel that you are in. " +
                                    "Requires the `Connect` permission in the voice channel's permission settings.");
                            break;
                        case "leave":
                            embed.setDescription("Leaves any voice channel that the Jukebox is in.");
                            break;
                        case "lyrics":
                            embed.setDescription("Searches the lyrics for the current track (if any) or search query provided.");
                            break;
                        case "nowplaying":
                            embed.setDescription("Displays information about the current track (if any), as well as the next track in queue.");
                            break;
                        case "pause":
                            embed.setDescription(String.format("Pauses the current track (if any). " +
                                    "Use the `%splay` command to unpause the track.", Jukebox.getPrefix()));
                            break;
                        case "ping":
                            embed.setDescription("Returns the latency of the Jukebox.");
                            break;
                        case "play":
                            embed.setDescription(
                                    """
                                    Plays a track given through a URL or search query. If no track is given, the Jukebox will continue playback if paused/stopped.

                                    The bot currently **supports** playing from these streaming services:
                                    - YouTube (videos/playlists)
                                    - Soundcloud (tracks only)
                                    - Spotify (tracks, albums, artists, and playlists)
                                    """);
                            break;
                        case "queue":
                            embed.setDescription("Displays all the tracks that are currently in the queue.");
                            embed.addField("Subcommands",
                                    String.format(
                                            """
                                            `%1$squeue remove <track position>` - Removes a track from the queue.
                                            `%1$squeue move <old position> <new position>` - Moves a track to a new position in queue.
                                            `%1$squeue next <track position>` - Moves a track to the first position in queue.
                                            """
                                            , Jukebox.getPrefix()),
                                    true);
                            break;
                        case "repeat":
                            embed.setDescription(String.format(
                                    """
                                    Changes the repeat state of the Jukebox.

                                    There are three states that the player can be set to:
                                    `%1$srepeat track` - This will continuously loop the current track.
                                    `%1$srepeat queue` - This will continuously loop all the tracks in the queue. After a track is played, it will be readded at the end of the queue.
                                    `%1$srepeat off` - Turns off the repeat functions of the Jukebox.
                                    """,
                                    Jukebox.getPrefix()));
                            break;
                        case "skip":
                            embed.setDescription("Skips the current track (if any) and plays the next track in the queue.");
                            break;
                        case "shuffle":
                            embed.setDescription("Shuffles all the tracks in the queue.");
                            break;
                        case "stop":
                            embed.setDescription(String.format("Stops the current track (if any). " +
                                    "If the `%splay` command is used, the track will play again from the beginning.", Jukebox.getPrefix()));
                            break;
                    }

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