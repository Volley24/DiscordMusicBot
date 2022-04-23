package max.utils;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import max.TrackScheduler;
import max.utils.AudioPlayerSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public class GuildAudioManager {
    public final Guild guild;
    public final AudioPlayer audioPlayer;
    public final TrackScheduler trackScheduler;

    public TextChannel musicLogChannel;

    public GuildAudioManager(Guild guild, AudioPlayerManager audioPlayerManager, TextChannel musicLogChannel){
        this.guild = guild;
        this.audioPlayer = audioPlayerManager.createPlayer();
        this.musicLogChannel = musicLogChannel;

        this.trackScheduler = new TrackScheduler(musicLogChannel, audioPlayer);
    }

    public void registerSendingHandler(){
        getAudioManager().setSendingHandler(getSendHandler());
    }

    public void send(String message, Object... args){
        musicLogChannel.sendMessageFormat(message, args).queue();
    }

    public AudioManager getAudioManager(){
        return guild.getAudioManager();
    }

    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(audioPlayer);
    }

    public void setMusicLogChannel(TextChannel musicLogChannel){
        this.musicLogChannel = musicLogChannel;
    }
}
