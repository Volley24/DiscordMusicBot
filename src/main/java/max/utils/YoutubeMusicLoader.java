package max.utils;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import java.util.regex.Pattern;

public abstract class YoutubeMusicLoader implements AudioLoadResultHandler{
    private static final Pattern YOUTUBE_VIDEO_ID = Pattern.compile("^[a-zA-Z0-9]{11}$", Pattern.CASE_INSENSITIVE);

    public final String query;
    public final boolean isRegularSearch;

    public YoutubeMusicLoader(String query){
        System.out.println("Call");
        this.query = query;
        this.isRegularSearch = query.startsWith("id:") || YOUTUBE_VIDEO_ID.matcher(query).matches();
    }

    public void search(AudioPlayerManager audioPlayerManager){
        System.out.println("On load");
        audioPlayerManager.loadItem(isRegularSearch ? query : "ytsearch:" + query, this);
    }
}
