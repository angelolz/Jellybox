package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import music.GuildMusicManager;
import music.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import structure.MusicTrack;

public class Stop extends Command
{
    public Stop()
    {
        this.name = "stop";
        this.help = "Stops any currently playing song.";
        this.cooldown = 3;
    }

    @Override
    protected void execute(CommandEvent commandEvent)
    {
        GuildVoiceState selfVoiceState = commandEvent.getSelfMember().getVoiceState();
        GuildVoiceState userVoiceState = commandEvent.getMember().getVoiceState();

        if(!selfVoiceState.inVoiceChannel())
            commandEvent.reply(":x: | I'm not even in a voice channel!");

        else if(!userVoiceState.inVoiceChannel())
            commandEvent.reply(":x: | You need to be in a voice channel to use this command!");

        else if(selfVoiceState.inVoiceChannel() && !userVoiceState.getChannel().equals(selfVoiceState.getChannel()))
            commandEvent.reply(":x: | You need to be in the same voice channel as me for this command to work!");

        else
        {
            GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(commandEvent.getGuild());

            // add the track back into the queue so that it can be replayed again
            AudioTrack track = musicManager.getScheduler().getPlayer().getPlayingTrack();
            MusicTrack lastRequestTrack = new MusicTrack(track.makeClone(), musicManager.getRequester());

            if(track != null)
            {
                musicManager.getScheduler().getQueue().add(0, lastRequestTrack);

                //stop the track
                musicManager.getScheduler().getPlayer().stopTrack();

                commandEvent.reply(":stop_button: | The currently playing track has been stopped.");
            }

            else
                commandEvent.reply(":x: | There is no song playing!");
        }
    }
}
