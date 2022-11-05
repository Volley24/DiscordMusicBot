package max.music;


import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import max.Bot;
import max.Secrets;
import max.music.audio.TrackScheduler;
import max.music.audio.GuildAudioManager;
import max.music.audio.YoutubeMusicLoader;
import max.music.utils.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MusicBot extends Bot {

    private enum VoiceStatus {
        IN_SAME_VC,
        IN_DIFFERENT_VC,
        NOT_CONNECTED
    }

    public enum CommandAccessorType {
        ANYONE,             // Anyone can execute this command, regardless of voice status.
        NOT_IN_VC,          // Can only be executed if the bot is not currently in a VC.
        IN_SAME_VC_OR_JOIN, // You must be in the same vc as the bot OR if the bot is not in a vc, it joins your vc.
        IN_SAME_VC          // You must be in the same vc as the bot. (If bot is not in a vc, command doesn't execute)
    }

    private final AudioPlayerManager audioPlayerManager;
    private final HashMap<String, GuildAudioManager> guildAudioManagers;

    private TimerTask leaveTimerTask = null;
    private TimerTask clearQueueTimeTask = null;

    public MusicBot(JDA jda){
        super(jda);

        this.audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);

        this.guildAudioManagers = new HashMap<>();
    }

    @Override
    public void onSlashCommand(@NotNull  SlashCommandEvent event) {
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

        Member member = event.getMember();
        TrackScheduler trackScheduler = guildAudioManager.trackScheduler;

        if(commandEquals(event, "help", guildAudioManager, CommandAccessorType.ANYONE)){
            event.reply("*Here is a list of all commands:**\n\n" +

                    "/queue - Open the queue which allows you to use buttons to control some things.\n\n"+

                    "/join - Makes me join the VC your are in\n"+
                    "/dc - Makes me leave the VC I am in.\n\n"+

                    "/add [song-name] - Add a song to the queue\n"+
                    "/remove [song-position] - Remove a song from a position inside the queue\n"+
                    "/playnow [song-name] - Stops playing current song, inserts new song at current position, and plays that song.\n"+
                    "/clear - Remove all the songs from the queue\n\n"+

                    "/volume [volume] - Change the playback volume of the music\n"+
                    "/loopmode [loop-mode] - Change the loop-mode of the music bot. Options: NONE, LOOP_SONG, and LOOP_QUEUE.\n\n"+

                    "/skip - Skip the currently playing song and play the next one.\n"+
                    "/prev - Skip the currently playing song and play the previous one.\n"
                    ).queue();

        }else if(commandEquals(event, "add", guildAudioManager, CommandAccessorType.IN_SAME_VC_OR_JOIN)){
            System.out.println("Add song");
            String query = event.getOption("song-name").getAsString();

            queueTrack(guildAudioManager, member, query, false);
            event.reply("**Searching youtube for:** \"" + query + "\"...").queue();

        }else if(commandEquals(event, "remove", guildAudioManager, CommandAccessorType.IN_SAME_VC)){
            try{
                String bruh = String.valueOf(event.getOption("song-position").getAsDouble());
                if(bruh.endsWith(".0")){
                    bruh = bruh.substring(0, bruh.length()-2);
                }
                int songIndexToRemove = Integer.parseInt(bruh) - 1;
                if (songIndexToRemove >= 0 && songIndexToRemove < trackScheduler.getSongAmount()){
                    trackScheduler.removeSongFromQueue(songIndexToRemove);
                    event.reply("Successfully removed song at position **" + (songIndexToRemove + 1) +"**").queue();
                }else{
                    event.reply("Sorry, but the song position must be between 1 and the size of the queue!").setEphemeral(true).queue();
                }

            }catch (NumberFormatException e){
                event.reply("Sorry, but the song position must be a valid non-decimal integer!").setEphemeral(true).queue();
            }

        }else if(commandEquals(event, "clear", guildAudioManager, CommandAccessorType.IN_SAME_VC)){
            trackScheduler.clear();
            event.reply("The entire queue has been successfully cleared.").queue();

        }else if(commandEquals(event, "volume", guildAudioManager, CommandAccessorType.IN_SAME_VC)){
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
        }else if(commandEquals(event, "prev", guildAudioManager, CommandAccessorType.IN_SAME_VC)){
            event.reply("**Playing previous song...**").queue();
            trackScheduler.skipAndPlay(-1);

        }else if(commandEquals(event, "skip", guildAudioManager, CommandAccessorType.IN_SAME_VC)){
            event.reply("**Playing next song...**").queue();
            trackScheduler.skipAndPlay(1);

        }else if(commandEquals(event, "playnow", guildAudioManager, CommandAccessorType.IN_SAME_VC_OR_JOIN)){
            String query = event.getOption("song-name").getAsString();

            event.reply("**Searching youtube for:** " + query + "...").queue();
            queueTrack(guildAudioManager, member, query, true);

        }else if(commandEquals(event, "queue", guildAudioManager, CommandAccessorType.ANYONE)){
            event.reply("**Here is the queue:**").queue();

        }else if(commandEquals(event, "join", guildAudioManager, CommandAccessorType.ANYONE)){
            boolean successfullyJoinedVC = joinVoiceChannel(guildAudioManager, member);

            if(successfullyJoinedVC){
                event.reply("Successfully joined voice channel!").queue();
            }else{
                event.reply("You don't seem to be connected to any voice channel!").queue();
            }

        }else if(commandEquals(event, "dc", guildAudioManager, CommandAccessorType.IN_SAME_VC)){
            guildAudioManager.getAudioManager().closeAudioConnection();
            event.reply("Successfully disconnected from voice channel.").queue();
            return;
        }
        trackScheduler.sendQueue();
    }


    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        Guild guild = event.getGuild();
        GuildAudioManager guildAudioManager = this.getGuildAudioManager(guild);

        AudioManager audioManager = guildAudioManager.getAudioManager();

        if (audioManager.getConnectedChannel() != null && audioManager.getConnectedChannel().getId().equals(event.getChannelJoined().getId())){
            // Our VC
            if (leaveTimerTask != null){
                leaveTimerTask.cancel();
            }

            if (clearQueueTimeTask != null) {
                clearQueueTimeTask.cancel();
            }
        }
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        Guild guild = event.getGuild();
        GuildAudioManager guildAudioManager = this.getGuildAudioManager(guild);

        AudioManager audioManager = guildAudioManager.getAudioManager();

        if (audioManager.getConnectedChannel() != null && audioManager.getConnectedChannel().getId().equals(event.getChannelLeft().getId())){
            //VC is ours
            if(event.getChannelLeft().getMembers().size() == 1){
                //Only us, so pause
                guildAudioManager.trackScheduler.getPlayer().setPaused(true);
                guildAudioManager.send("Since everyone has left the voice channel, the player has automatically been **paused**. I will disconnect from the VC in %s minutes and clear the queue in %s minutes", Constants.LEAVE_TIME, Constants.QUEUE_CLEAR_TIME);

                if (leaveTimerTask != null) {
                    leaveTimerTask.cancel();
                }

                if (clearQueueTimeTask != null) {
                    clearQueueTimeTask.cancel();
                }

                leaveTimerTask = new TimerTask() {
                    public void run() {
                        audioManager.closeAudioConnection();
                        guildAudioManager.send("I have **disconnected** from the VC after %s minutes of inactivity.", Constants.LEAVE_TIME);
                    }
                };

                clearQueueTimeTask = new TimerTask() {
                    public void run() {
                        guildAudioManager.trackScheduler.clear();
                        guildAudioManager.send("The queue has automatically been **cleared** after %s minutes of inactivity.", Constants.QUEUE_CLEAR_TIME);

                        // If queue is cleared, we set player to resume so upon them adding another song
                        // It will automatically play
                        guildAudioManager.trackScheduler.getPlayer().setPaused(false);
                    }
                };

                new Timer("Timer-Leave").schedule(leaveTimerTask, Constants.LEAVE_TIME * 1000 * 60);
                new Timer("Timer-Clear_Queue").schedule(clearQueueTimeTask, Constants.QUEUE_CLEAR_TIME * 1000 * 60);

            }
        }else if(event.getMember().equals(event.getGuild().getSelfMember())) {
            guildAudioManager.trackScheduler.getPlayer().setPaused(true);
        }
    }



    private boolean commandEquals(SlashCommandEvent event, String label, GuildAudioManager guildAudioManager, CommandAccessorType commandAccessorType){
        String commandLabel = event.getName();
        Member member = event.getMember();

        if(commandLabel.equals(label)){
            if (commandAccessorType == CommandAccessorType.ANYONE)
                return true;

            switch (getMemberVoiceStatus(guildAudioManager, member)) {
                case IN_SAME_VC:
                    return true;


                case NOT_CONNECTED:
                    if(commandAccessorType == CommandAccessorType.IN_SAME_VC){
                        event.reply("Sorry, but you can only execute this command when I am in a voice channel!").queue();
                    }else if(commandAccessorType == CommandAccessorType.IN_SAME_VC_OR_JOIN){
                        boolean successfullyJoinedVC = joinVoiceChannel(guildAudioManager, member);

                        if(successfullyJoinedVC){
                            event.reply("Successfully joined voice channel!").queue();
                        }else{
                            event.reply("You don't seem to be connected to any voice channel!").queue();
                        }

                        return successfullyJoinedVC;
                    }else if (commandAccessorType == CommandAccessorType.NOT_IN_VC){
                        event.reply("Sorry, but I am already connected to a voice channel!").queue();
                        return !guildAudioManager.getAudioManager().isConnected();
                    } else{
                        return true;
                    }
                break;

                case IN_DIFFERENT_VC: event.reply("Sorry, but you must be in the same voice channel as me to execute this command!").queue(); break;
            }
        }
        return false;
    }

    private void queueTrack(GuildAudioManager guildAudioManager, Member member, String query, boolean playNow){
        new YoutubeMusicLoader(query) {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (playNow) {
                    guildAudioManager.trackScheduler.playNowAndInsert(track);
                }else{
                    guildAudioManager.trackScheduler.addSongToQueue(track);
                    guildAudioManager.send("**%s** added the track to the queue: %s.", member.getEffectiveName(), Utils.formatAudioTrack(track, false));
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if(!this.isRegularSearch){
                    AudioTrack audioTrack = playlist.getTracks().get(0);

                    if (playNow) {
                        guildAudioManager.trackScheduler.playNowAndInsert(audioTrack);
                    }else{
                        guildAudioManager.trackScheduler.addSongToQueue(audioTrack);
                        guildAudioManager.send("**%s** added the track to the queue: %s.", member.getEffectiveName(), Utils.formatAudioTrack(audioTrack, false));
                    }
                }else{
                    if (playNow) {
                        guildAudioManager.send("Sorry, but /playnow only accepts songs and not playlists.");
                    }else {
                        for (AudioTrack audioTrack : playlist.getTracks()) {
                            guildAudioManager.trackScheduler.addSongToQueue(audioTrack);
                        }
                        guildAudioManager.send("**%s** added the playlist to the queue: %s.", member.getEffectiveName(), playlist.getName());
                    }
                }
            }

            @Override
            public void noMatches() {
                guildAudioManager.send("No matches were found for: '%s'", query);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                guildAudioManager.send("Could not load track: '%s'", query);
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
            guildAudioManager = new GuildAudioManager(guild, audioPlayerManager, guild.getTextChannelById(Secrets.COMMAND_CHANNEL));
            guildAudioManagers.put(guild.getId(), guildAudioManager);
        }

        setSendingHandler(guildAudioManager);
        return guildAudioManager;
    }

    private void setSendingHandler(GuildAudioManager guildAudioManager){
        guildAudioManager.getAudioManager().setSendingHandler(guildAudioManager.getSendHandler());
    }
}
