package max.music.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;


public abstract class YoutubeMusicLoader implements AudioLoadResultHandler{
    public final String query;
    public final boolean isRegularSearch;

    public YoutubeMusicLoader(String query){
        this.query = query;
        this.isRegularSearch = query.startsWith("id:") || query.contains("youtube.com") || query.contains("spotify.com");
    }

    public void search(AudioPlayerManager audioPlayerManager){
        System.out.println(isRegularSearch);
        audioPlayerManager.loadItem(isRegularSearch ? query : "ytsearch:" + query, this);
    }
}
