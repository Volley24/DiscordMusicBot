package max;


import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import max.utils.GuildAudioManager;
import max.utils.Logger;
import max.utils.Utils;
import max.utils.YoutubeMusicLoader;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MusicBot extends ListenerAdapter {

    private enum VoiceStatus {
        IN_SAME_VC,
        IN_DIFFERENT_VC,
        NOT_CONNECTED
    }

    private enum CommandAccessorType {
        ANYONE,             // Anyone can execute this command, regardless of voice status.
        NOT_IN_VC,          // Can only be executed if the bot is not currently in a VC.
        IN_SAME_VC_OR_JOIN, // You must be in the same vc as the bot OR if the bot is not in a vc, it joins your vc.
        IN_SAME_VC          // You must be in the same vc as the bot. (If bot is not in a vc, command doesn't execute)
    }

    private final AudioPlayerManager audioPlayerManager;
    private final HashMap<String, GuildAudioManager> guildAudioManagers;

    public MusicBot(JDA jda){
        jda.addEventListener(this);

        this.audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);

        this.guildAudioManagers = new HashMap<>();
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (event.getGuild() == null || event.getMember() == null){
            return;
        }

        Guild guild = event.getGuild();
        GuildAudioManager guildAudioManager = this.getGuildAudioManager(guild);

        if(!event.getChannel().getId().equals(guildAudioManager.trackScheduler.getCommandsChannel().getId())){
            event.replyFormat("Please go to %s to use my commands.", guildAudioManager.trackScheduler.getCommandsChannel().getAsMention()).setEphemeral(true).queue();
            return;
        }

        Logger.log("\nSlash command received", Logger.LogType.INFO);

        String commandLabel = event.getName();
        Member member = event.getMember();
        TrackScheduler trackScheduler = guildAudioManager.trackScheduler;

        if(commandEquals(commandLabel, "help", guildAudioManager, member, CommandAccessorType.ANYONE)){
            event.reply("""
                    **Here is a list of all commands:**

                    /queue - Open the queue which allows you to use buttons to control some things.

                    /join - Makes me join the VC your are in
                    /dc - Makes me leave the VC I am in.

                    /add [song-name] - Add a song to the queue
                    /remove [song-position] - Remove a song from a position inside the queue
                    /playnow [song-name] - Stops playing current song, inserts new song at current position, and plays that song.
                    /clear - Remove all the songs from the queue

                    /volume [volume] - Change the playback volume of the music
                    /loopmode [loop-mode] - Change the loop-mode of the music bot. Options: NONE, LOOP_SONG, and LOOP_QUEUE.

                    /skip - Skip the currently playing song and play the next one.
                    /prev - Skip the currently playing song and play the previous one.""").queue();

        }else if(commandEquals(commandLabel, "add", guildAudioManager, member, CommandAccessorType.IN_SAME_VC_OR_JOIN)){
            System.out.println("Add song");
            String query = event.getOption("song-name").getAsString();

            queueTrack(guildAudioManager, member, query, false);
            event.reply("**Searching youtube for:** " + query + "...").queue();

        }else if(commandEquals(commandLabel, "remove", guildAudioManager, member, CommandAccessorType.IN_SAME_VC)){
            try{
                String bruh = String.valueOf(event.getOption("song-position").getAsDouble());
                if(bruh.endsWith(".0")){
                    bruh = bruh.substring(0, bruh.length()-2);
                }
                int songIndexToRemove = Integer.parseInt(bruh) - 1;
                if (songIndexToRemove >= 0 && songIndexToRemove < trackScheduler.getSongAmount()){
                    trackScheduler.remove(songIndexToRemove);
                    event.reply("Successfully removed song at position **" + (songIndexToRemove + 1) +"**").queue();
                }else{
                    event.reply("Sorry, but the song position must be between 1 and the size of the queue!").setEphemeral(true).queue();
                }

            }catch (NumberFormatException e){
                event.reply("Sorry, but the song position must be a valid non-decimal integer!").setEphemeral(true).queue();
            }

        }else if(commandEquals(commandLabel, "clear", guildAudioManager, member, CommandAccessorType.IN_SAME_VC)){
            trackScheduler.clear();
            event.reply("The entire queue has been successfully cleared.").queue();

        }else if(commandEquals(commandLabel, "volume", guildAudioManager, member, CommandAccessorType.IN_SAME_VC)){
            try{
                String bruh = String.valueOf(event.getOption("volume").getAsDouble());
                if(bruh.endsWith(".0")){
                    bruh = bruh.substring(0, bruh.length()-2);
                }
                int volume = Integer.parseInt(bruh);
                if (volume >= 0 && volume <= 100){
                    trackScheduler.getPlayer().setVolume(volume);
                    event.reply("Successfully set volume to **" + volume+"/100**").queue();
                }else{
                    event.reply("Sorry, but the volume must be between 0 and 100!").setEphemeral(true).queue();
                }

            }catch (NumberFormatException e){
                event.reply("Sorry, but the volume must be a valid non-decimal integer!").setEphemeral(true).queue();
            }
        }else if(commandEquals(commandLabel, "prev", guildAudioManager, member, CommandAccessorType.ANYONE)){
            event.reply("**Playing previous song...**").queue();
            trackScheduler.skipAndPlay(-1);

        }else if(commandEquals(commandLabel, "skip", guildAudioManager, member, CommandAccessorType.ANYONE)){
            event.reply("**Playing next song...**").queue();
            trackScheduler.skipAndPlay(1);

        }else if(commandEquals(commandLabel, "playnow", guildAudioManager, member, CommandAccessorType.IN_SAME_VC_OR_JOIN)){
            String query = event.getOption("song-name").getAsString();

            event.reply("**Searching youtube for:** " + query + "...").queue();
            queueTrack(guildAudioManager, member, query, true);

        }else if(commandEquals(commandLabel, "queue", guildAudioManager, member, CommandAccessorType.ANYONE)){
            event.reply("**Here is the queue:**").queue();

        }else if(commandEquals(commandLabel, "join", guildAudioManager, member, CommandAccessorType.ANYONE)){
            boolean successfullyJoinedVC = joinVoiceChannel(guildAudioManager, member);

            if(successfullyJoinedVC){
                event.reply("Successfully joined voice channel!").queue();
            }else{
                event.reply("You don't seem to be connected to any voice channel!").queue();
            }

        }else if(commandEquals(commandLabel, "dc", guildAudioManager, member, CommandAccessorType.IN_SAME_VC)){
            guildAudioManager.getAudioManager().closeAudioConnection();
            event.reply("Successfully disconnected from voice channel.").queue();
            return;
        }
        trackScheduler.sendQueue();
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        Guild guild = event.getGuild();
        GuildAudioManager guildAudioManager = this.getGuildAudioManager(guild);

        AudioManager audioManager = guildAudioManager.getAudioManager();

        if (audioManager.getConnectedChannel() != null &&audioManager.getConnectedChannel().getId().equals(event.getChannelLeft().getId())){
            //VC is ours
            if(event.getChannelLeft().getMembers().size() == 1){
                //Only us, so pause
                guildAudioManager.trackScheduler.getPlayer().setPaused(true);
                guildAudioManager.send("Since everyone has left the voice channel, the player has automatically been **paused**.");
            }
        }else if(event.getMember().equals(event.getGuild().getSelfMember())){
            //Whoops, we dc'd
            guildAudioManager.trackScheduler.getPlayer().setPaused(true);
            guildAudioManager.send("Since I have been removed from the voice channel, the player has automatically been **paused**.");
        }
    }



    private boolean commandEquals(String commandLabel, String label, GuildAudioManager guildAudioManager, Member member, CommandAccessorType commandAccessorType){
        if(commandLabel.equals(label)){
            if (commandAccessorType == CommandAccessorType.ANYONE)
                return true;

            switch (getMemberVoiceStatus(guildAudioManager, member)) {
                case IN_SAME_VC -> {
                    return true;
                }

                case NOT_CONNECTED -> {
                    if(commandAccessorType == CommandAccessorType.IN_SAME_VC){
                        guildAudioManager.send("Sorry, but you can only execute this command when I am in a voice channel!");
                    }else if(commandAccessorType == CommandAccessorType.IN_SAME_VC_OR_JOIN){
                        boolean successfullyJoinedVC = joinVoiceChannel(guildAudioManager, member);

                        if(successfullyJoinedVC){
                            guildAudioManager.send("Successfully joined voice channel!");
                        }else{
                            guildAudioManager.send("You don't seem to be connected to any voice channel!");
                        }

                        return successfullyJoinedVC;
                    }else if (commandAccessorType == CommandAccessorType.NOT_IN_VC){
                        guildAudioManager.send("Sorry, but I am already connected to a voice channel!");
                        return !guildAudioManager.getAudioManager().isConnected();
                    } else{
                        return true;
                    }
                }

                case IN_DIFFERENT_VC -> guildAudioManager.send("Sorry, but you must be in the same voice channel as me to execute this command!");
            }
        }
        return false;
    }

    private void queueTrack(GuildAudioManager guildAudioManager, Member member, String query, boolean playNow){
        System.out.println("Queue");
        new YoutubeMusicLoader(query) {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (playNow) {
                    guildAudioManager.trackScheduler.playNowAndInsert(track);
                }else{
                    guildAudioManager.trackScheduler.queue(track);
                    guildAudioManager.send(member.getEffectiveName() + " added the track to the queue: %s.", Utils.formatAudioTrack(track, false));
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if(!this.isRegularSearch){
                    AudioTrack audioTrack = playlist.getTracks().get(0);

                    if (playNow) {
                        guildAudioManager.trackScheduler.playNowAndInsert(audioTrack);
                    }else{
                        guildAudioManager.trackScheduler.queue(audioTrack);
                        guildAudioManager.send(member.getEffectiveName() + " added the track to the queue: %s.", Utils.formatAudioTrack(audioTrack, false));
                    }
                }else{
                    if (playNow) {
                        guildAudioManager.send("Sorry, but /playnow only accepts songs and not playlist.");
                    }else {
                        for (AudioTrack audioTrack : playlist.getTracks()) {
                            guildAudioManager.trackScheduler.queue(audioTrack);
                        }
                        guildAudioManager.send(member.getEffectiveName() + " added the playlist to the queue: %s.", playlist.getName());
                    }
                }
            }

            @Override
            public void noMatches() {
                guildAudioManager.send("No matches were found for: '"+query+"'");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                guildAudioManager.send("Could not load track: '"+query+"'");
            }
        }.search(audioPlayerManager);
    }

    private boolean joinVoiceChannel(GuildAudioManager guildAudioManager, Member member){
        AudioChannel memberAudioChannel = member.getVoiceState().getChannel();

        if(memberAudioChannel == null){
            return false;
        }
        AudioManager audioManager = guildAudioManager.getAudioManager();

        audioManager.setSelfDeafened(true);
        audioManager.openAudioConnection(memberAudioChannel);

        guildAudioManager.registerSendingHandler();
        return true;

    }

    private VoiceStatus getMemberVoiceStatus(GuildAudioManager guildAudioManager, Member member){
        if(!guildAudioManager.getAudioManager().isConnected()) {
            return VoiceStatus.NOT_CONNECTED;
        }else{
            Member selfMember = member.getGuild().getSelfMember();

            return selfMember.getVoiceState().getChannel() == member.getVoiceState().getChannel()
                    ? VoiceStatus.IN_SAME_VC
                    : VoiceStatus.IN_DIFFERENT_VC;
        }
    }


    private GuildAudioManager getGuildAudioManager(Guild guild){
        GuildAudioManager guildAudioManager = guildAudioManagers.get(guild.getId());

        if(guildAudioManager == null){
            System.out.println("Making new GuildAudioManager...");
            guildAudioManager = new GuildAudioManager(guild, audioPlayerManager, guild.getTextChannelsByName("commands",true).get(0));
            guildAudioManagers.put(guild.getId(), guildAudioManager);
        }

        setSendingHandler(guildAudioManager);
        return guildAudioManager;
    }

    private void setSendingHandler(GuildAudioManager guildAudioManager){
        guildAudioManager.getAudioManager().setSendingHandler(guildAudioManager.getSendHandler());
    }
}
