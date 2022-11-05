package max.music.utils;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

public class Utils {
    public static String formatAudioTrack(AudioTrack audioTrack, boolean playing){
        if(audioTrack == null)
            return "*Nothing*";

        AudioTrackInfo audioTrackInfo = audioTrack.getInfo();
        String time;
        if(playing){
            time = formatTime(audioTrack.getPosition()) + " / " + formatTime(audioTrack.getDuration());
        }else{
            time = formatTime(audioTrack.getDuration());
        }
        return String.format("%s **BY** %s - [%s]", audioTrackInfo.title, audioTrackInfo.author, time);
    }

    public static String formatTime(long millis){
        long totalSeconds = millis / 1000;

        long hours = totalSeconds / 3600;

        long secondsInMinutes = totalSeconds - (hours * 3600);
        long minutes = secondsInMinutes / 60;
        long seconds = secondsInMinutes % 60;

        return (hours != 0 ? hours + ":" : "") + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }
}
