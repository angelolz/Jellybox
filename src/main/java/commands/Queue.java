package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import music.GuildMusicManager;
import music.PlayerManager;
import music.TrackScheduler;
import net.dv8tion.jda.api.entities.MessageChannel;
import structure.MusicTrack;

import java.util.LinkedList;

public class Queue extends Command{

    public Queue(){
        this.name = "queue";
        this.help = "`!queue`, `!queue`: Returns list of song in the queue\n";
        this.aliases = new String[] {"q"};
        this.cooldown = 3;
    }

    @Override
    protected void execute(CommandEvent event){
        GuildMusicManager manager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        TrackScheduler scheduler = manager.getScheduler();
        MessageChannel channel = event.getChannel();
        LinkedList<MusicTrack> ts = null;

        try{
            String[] args = event.getArgs().split("\\s+"); //Split arguments into array of strings
            StringBuilder sb = new StringBuilder(); //StringBuilder for printing out list of song currently in the queue
            ts = manager.getScheduler().getQueue(); //Linked list containing the current queue
            
            if(manager.getScheduler().getQueue().isEmpty()){ //If queue is empty
                event.reply(":x: | The queue is empty!");
            }
            else if(args[0] == "list" || event.getArgs() == ""){ //If user uses "list" sub-command or no sub-command is specified
                int trackNumber = 1;
                event.reply(":musical_note: | Songs in queue:");
                for(MusicTrack mTrack : ts){
                    sb.append(trackNumber + " " + mTrack.getTrack().getInfo().title + ", " + mTrack.getTrack().getInfo().author);
                    trackNumber++;
                }
                channel.sendMessage(sb).queue();
            }     
            else if(args[0] == "remove"){ //If user uses "remove" sub-command.
                if(args.length == 2){
                    try{
                        String trackName = scheduler.getQueue().get(Integer.parseInt(args[1]) - 1).getTrack().getInfo().title;
                        scheduler.getQueue().remove(Integer.parseInt(args[1]) - 1);
                        event.reply(":wastebasket: | Song removed from queue: " + trackName);
                    }
                    catch(IndexOutOfBoundsException e){
                        event.reply(":x: | That track number does not exist!");
                    }
                }
                else{ //If no track number is given or user does not use correct format.
                    event.reply(":x: | Usage: `!queue remove <track number>`");
                }
            }   
            // else if(args[0] == "next"){ //If user uses the "next" sub-command
            //     if(args.length == 2){
            //         AudioTrack at = new AudioTrack() {
                        
            //         };
            //     }
            //     else{ //If user applies too many arguments
            //         event.reply(":x: | Usage: `!queue next <song url>`");
            //     }
            // }
            else if(args[0] == "move"){
                if(args.length == 3){
                    try{
                        MusicTrack track = scheduler.getQueue().get(Integer.parseInt(args[1])); //Save the track to be moved.
                        scheduler.getQueue().remove(Integer.parseInt(args[1]) - 1); //Remove track from original position.
                        scheduler.getQueue().add(Integer.parseInt(args[2]), track); //Move track to new position.
                    }
                    catch(IndexOutOfBoundsException e){
                        event.reply(":x: | That track number or new position does not exist!");
                        event.reply(":x: | Usage: `!queue move <track number> <new position>`");
                    }
                }
                else{ //If no index is given or user does not use correct format
                    event.reply(":x: | Usage: `!queue move <track number> <new position number>`");
                }
            }
            else{
                event.reply(":x: | invalid argument(s), type `!help` to see list of commands!");
            }
        }
        catch(Exception e){
            event.reply(":x: | There was a problem!");
        }
    }
}