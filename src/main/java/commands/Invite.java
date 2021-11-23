package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

public class Invite extends Command
{
    public Invite()
    {
        this.name = "invite";
        this.ownerCommand = true;
        this.hidden = true;
    }

    @Override
    protected void execute(CommandEvent commandEvent)
    {
        EmbedBuilder embed = new EmbedBuilder().setColor(0x1ed760);
        embed.setDescription("Invite the bot **[here](https://discord.com/api/oauth2/authorize?client_id=890344093824204810&permissions=2150714368&scope=bot)**!");
        commandEvent.reply(embed.build());
    }
}
