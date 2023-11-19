package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import main.Jukebox;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import utils.Statics;

import java.util.*;

public class Help extends Command
{

    public Help()
    {
        this.name = "help";
        this.arguments = "[cmd name]";
        this.help = "Shows you a list of the available commands.";
        this.cooldown = 3;

        this.category = new Category("Bot");
    }

    @Override
    protected void execute(CommandEvent event)
    {
        MessageChannel channel = event.getChannel();

        if(!event.getArgs().isEmpty())
        {
            EmbedBuilder embed = new EmbedBuilder().setColor(Statics.EMBED_COLOR);

            // goes through all command to find a match
            for(Command command : event.getClient().getCommands())
            {
                String[] aliases = command.getAliases();

                if(commandMatches(command.getName(), aliases, event.getArgs()))
                {
                    embed.setTitle(String.format("Command Info for `%s%s`", Jukebox.getPrefix(), command.getName()))
                         .addField("Arguments", command.getArguments() == null ? "*None*" : command.getArguments(), true)
                         .addField("Cooldown Time", String.valueOf(command.getCooldown()), true);

                    //convert aliases to list object, so it can print it out nicely
                    List<String> list = Arrays.asList(command.getAliases());
                    String stringList = String.join(", ", list);
                    embed.addField("Aliases", stringList.isEmpty() ? "*None*" : stringList, true);

                    getCommandHelp(embed, command.getName());

                    event.reply(embed.build());
                    return;
                }
            }

            // if unable to find a match, show error message
            event.reply(":x: | Sorry, there's no command that matches that name!");
        }

        else
        {
            EmbedBuilder embed = new EmbedBuilder()
                .setColor(Statics.EMBED_COLOR)
                .setTitle("Player Controls");

            getCommands(embed, "player");

            channel.sendMessageEmbeds(embed.build()).setActionRow(getCategoryButtons(event.getAuthor().getId(), "player")).queue();
        }
    }

    public static void getEmbed(ButtonInteractionEvent event, String category)
    {
        EmbedBuilder embed = new EmbedBuilder().setColor(Statics.EMBED_COLOR);
        switch(category)
        {
            case "player" ->
            {
                embed.setTitle("Player Controls");
                getCommands(embed, "player");
            }
            case "bot" ->
            {
                embed.setTitle("Bot Commands");
                getCommands(embed, "bot");
            }
            case "tool" ->
            {
                embed.setTitle("Other Tools");
                getCommands(embed, "tool");
            }
            default -> Jukebox.getLogger().error("Unknown embed category name: {}", category);
        }

        event.deferEdit().setEmbeds(embed.build()).setActionRow(getCategoryButtons(event.getMember().getId(), category)).queue();

    }

    private static boolean commandMatches(String commandName, String[] aliases, String input)
    {
        return commandName.equalsIgnoreCase(input) || (aliases != null && Arrays.stream(aliases).anyMatch(a -> a.equalsIgnoreCase(input)));
    }

    private static void getCommands(EmbedBuilder embed, String category)
    {
        List<Command> commandsInCategory = Jukebox.getClient().getCommands().stream().filter(c -> c.getCategory().getName().equals(category)).toList();
        for(Command command : commandsInCategory)
        {
            String commandName = String.format("%s%s", Jukebox.getPrefix(), command.getName());
            if(command.getAliases().length > 0)
            {
                String[] aliases = command.getAliases();
                for(String alias : aliases)
                    commandName = commandName.concat("/" + alias);
            }
            embed.addField(commandName, command.getHelp(), true);
        }
        embed.setFooter(String.format("Version %s | Uptime: %s", Jukebox.getVersion(), Jukebox.getUptime()));
    }

    private static void getCommandHelp(EmbedBuilder embed, String commandName)
    {
        switch(commandName)
        {
            case "help" ->
                embed.setDescription("Shows an embed listing all of the available commands as well as detailed help for each of them. " +
                    "If you provide a command name or its alias as an argument, it would show the usage of the command in more detail.");
            case "join" -> embed.setDescription("Joins the same voice channel that you are in. " +
                "Requires the `Connect` permission in the voice channel's permission settings.");
            case "leave" -> embed.setDescription("Leaves any voice channel that the Jukebox is in.");
            case "nowplaying" ->
                embed.setDescription("Displays information about the current track (if any), as well as the next track in queue.");
            case "pause" -> embed.setDescription(String.format("Pauses the current track (if any). " +
                "Use the `%splay` command to unpause the track.", Jukebox.getPrefix()));
            case "ping" -> embed.setDescription("Returns the latency of the Jukebox.");
            case "play" -> embed.setDescription("""
                Plays a track given through a URL or search query. If no track is given, the Jukebox will continue playback if paused/stopped.
                The bot currently **supports** playing from these streaming services:
                - YouTube (videos/playlists)
                - Soundcloud (tracks only)
                - Spotify (tracks, albums, artists, and playlists)
                """);
            case "queue" -> embed.setDescription("Displays all the tracks that are currently in the queue.")
                                 .addField("Subcommands",
                                     String.format(
                                         """
                                             `%1$squeue remove <track position>` - Removes a track from the queue.
                                             `%1$squeue move <old position> <new position>` - Moves a track to a new position in queue.
                                             `%1$squeue next <track position>` - Moves a track to the first position in queue.
                                             """
                                         , Jukebox.getPrefix()),
                                     true);
            case "repeat" -> embed.setDescription(String.format(
                """
                    Changes the repeat state of the Jukebox.
                    There are three states that the player can be set to:
                    `%1$srepeat track` - This will continuously loop the current track.
                    `%1$srepeat queue` - This will continuously loop all the tracks in the queue. After a track is played, it will be readded at the end of the queue.
                    `%1$srepeat off` - Turns off the repeat functions of the Jukebox.
                    """,
                Jukebox.getPrefix()));
            case "skip" ->
                embed.setDescription("Skips the current track (if any) and plays the next track in the queue.");
            case "shuffle" -> embed.setDescription("Shuffles all the tracks in the queue.");
            case "stop" -> embed.setDescription(String.format("Stops the current track (if any). " +
                "If the `%splay` command is used, the track will play again from the beginning.", Jukebox.getPrefix()));
            default -> Jukebox.getLogger().error("Unknown command name: {}", commandName);
        }

    }

    private static List<Button> getCategoryButtons(String userId, String category)
    {
        List<Button> categoryButtons = new ArrayList<>();

        categoryButtons.add(Button.secondary(String.format("%s:pagination:help:player", userId), Emoji.fromUnicode("U+1F3B5")).withLabel("Player").withDisabled(category.equalsIgnoreCase("player")));
        categoryButtons.add(Button.secondary(String.format("%s:pagination:help:bot", userId), Emoji.fromUnicode("U+1F916")).withLabel("Bot").withDisabled(category.equalsIgnoreCase("bot")));
        categoryButtons.add(Button.secondary(String.format("%s:pagination:help:tool", userId), Emoji.fromUnicode("U+1F4C4")).asDisabled().withLabel("Tools").withDisabled(category.equalsIgnoreCase("tool")));

        return categoryButtons;
    }
}