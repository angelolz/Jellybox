package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import dev.lavalink.youtube.clients.Web;
import music.GuildMusicManager;
import music.PlayerManager;
import music.TrackScheduler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Admin extends Command
{
    public Admin()
    {
        this.name = "admin";
        this.ownerCommand = true;

        this.category = new Category("Admin");
    }

    @Override
    protected void execute(CommandEvent commandEvent)
    {
        String[] args = commandEvent.getArgs().split("\\s+");

        switch(args[0].toLowerCase())
        {
            case "servers" ->
            {
                StringBuilder sb = new StringBuilder();
                for(Guild guild : commandEvent.getJDA().getGuilds())
                    sb.append(guild.getName()).append(" - ").append("`").append(guild.getId()).append("`").append(" - Owner: ").append(guild.retrieveOwner().complete().getUser().getName()).append("\n");

                commandEvent.reply(sb.toString());
            }

            case "leave" ->
            {
                Stream<Guild> guildStream = commandEvent.getJDA().getGuilds().stream().filter(guild -> guild.getId().equals(args[1].trim()));
                List<Guild> guildLeftList = new ArrayList<>();

                guildStream.forEach(guild -> guild.leave().queue(s -> guildLeftList.add(guild)));

                if(guildLeftList.isEmpty())
                {
                    commandEvent.replyError("no servers found with that id.");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                for(Guild guild : guildLeftList)
                    sb.append(String.format("✅ left server: %s - %s", guild.getName(), guild.getId()));

                commandEvent.reply(sb.toString());
            }

            case "active" -> commandEvent.reply(getPlayersStatus(commandEvent.getJDA()));

            case "token" -> setTokens(args, commandEvent);
            default -> commandEvent.reply("what do u want loser `(servers, leave <guildId>, active)`");
        }
    }

    @NotNull
    private static String getPlayersStatus(JDA jda)
    {
        Map<Long, GuildMusicManager> musicManagers = PlayerManager.getInstance().getMusicManagers();
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<Long, GuildMusicManager> musicManager : musicManagers.entrySet())
        {
            TrackScheduler trackScheduler = musicManager.getValue().getScheduler();
            Guild guild = jda.getGuildById(musicManager.getKey());
            boolean connected = jda.getGuildById(musicManager.getKey()).getAudioManager().isConnected();
            sb.append(String.format("**%s** [`%s`] (Owner: %s)%n%s | %s | %s | %s songs in queue%n%n",
                guild.getName(),
                musicManager.getKey(),
                guild.retrieveOwner().complete().getUser().getName(),
                connected ? "**Connected**" : "*Not connected*",
                trackScheduler.getPlayer().getPlayingTrack() == null ? "⏹️ No track playing" : "▶️ Playing",
                trackScheduler.getPlayer().isPaused() ? "⏸️ Paused" : "▶️ Not paused",
                trackScheduler.getQueue().size()));
        }
        return sb.toString();
    }

    private static void setTokens(String[] args, CommandEvent event)
    {
        if(args.length < 2)
        {
            event.reply("usage: token <po_token> <visitor_data>");
            return;
        }

        Web.setPoTokenAndVisitorData(args[1], args[2]);
        event.getMessage().addReaction(Emoji.fromUnicode("U+2705")).queue();
    }
}
