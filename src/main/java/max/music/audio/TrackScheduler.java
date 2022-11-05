package max.music.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import max.music.interaction.MusicPanel;
import max.music.utils.Logger;
import max.music.utils.Utils;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;

public class TrackScheduler extends AudioEventAdapter {
    public enum LoopMode {
        OFF, SONG, QUEUE;

        public String getName(){
            String baseName = toString();
            return baseName.charAt(0) + baseName.substring(1).toLowerCase();
        }
    }
    private static final int SONGS_PER_PAGE = 6;

    private int failedAttempts = 0;

    private final TextChannel commandsChannel;
    private final AudioPlayer player;

    private final MusicPanel musicPanel;

    private LoopMode loopMode = LoopMode.OFF;

    public final ArrayList<AudioTrack> audioTracks;
    public int trackIndex = 0;

    public TrackScheduler(TextChannel commandsChannel, AudioPlayer audioPlayer) {
        this.commandsChannel = commandsChannel;
        this.player = audioPlayer;

        this.player.addListener(this);
        this.musicPanel = new MusicPanel(this);
        this.audioTracks = new ArrayList<>();

        Logger.log("Listener added for this guild...", Logger.LogType.INFO);
    }

    public TextChannel getCommandsChannel(){
        return commandsChannel;
    }

    public void sendQueue(){
        musicPanel.sendNew();
    }


    public AudioTrack getPlayingSong(){
        return player.getPlayingTrack();
    }

    public String getSongsOnPage(int page){
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < SONGS_PER_PAGE; i++){
            int realIndex = (page - 1) * SONGS_PER_PAGE + i;

            if (realIndex >= this.audioTracks.size())
                break;

            if (realIndex < trackIndex){
                stringBuilder.append("~~");
            }else if(realIndex == trackIndex){
                stringBuilder.append("**>>** ");
            }

            AudioTrack audioTrack = audioTracks.get(realIndex);

            stringBuilder.append("**#").append(realIndex + 1).append(":** ").append(Utils.formatAudioTrack(audioTrack, false));

            if (realIndex < trackIndex){
                stringBuilder.append("~~");
            }else if(realIndex == trackIndex){
                stringBuilder.append(" **<<**");
            }

            stringBuilder.append("\n");
        }

        if (page == getPageAmount()) {
            if (trackIndex == audioTracks.size()){
                stringBuilder.append(" **>>** End of Queue **<<**");
            }else{
                stringBuilder.append(" End of Queue ");
            }
        }

        return stringBuilder.toString();
    }

    public int getRemainingSongs(int page){
        if (getSongAmount() - (SONGS_PER_PAGE * (page - 1)) > SONGS_PER_PAGE) {
            return getSongAmount() - SONGS_PER_PAGE * page;
        }else{
            return 0;
        }
    }

    public int getPageAmount(){
        return (getSongAmount() - 1) / SONGS_PER_PAGE + 1;
    }

    public int getSongAmount(){
        return audioTracks.size();
    }


    public AudioPlayer getPlayer(){
        return player;
    }

    public LoopMode getLoopMode() {
        return loopMode;
    }

    public void setLoopMode(LoopMode loopMode){
        this.loopMode = loopMode;

        musicPanel.update();
    }


    /**
     * Clears the entire queue, resets the track index, and stops the currently playing song.
     */
    public void clear(){
        trackIndex = 0;
        audioTracks.clear();
        playNow(null);

        musicPanel.update();
    }


    /**
     * Adds a song to the end of the queue and starts playing it if no other song is queued.
     *
     * @param track AudioTrack to add to the queue
     */
    public void addSongToQueue(AudioTrack track) {
        Logger.log("Adding " + Utils.formatAudioTrack(track, true) + " to queue.", Logger.LogType.INFO);
        audioTracks.add(track);
        player.startTrack(track, true);

        musicPanel.update();
    }


    /**
     * Removes a song from a specified index.
     * If the songIndex to remove is the same as the index of the currently playing song, that song is replaced
     * with the new song at that index.
     *
     * @param songIndex The index of the song to remove.
     */
    public void removeSongFromQueue(int songIndex) {
        synchronized (audioTracks){
            audioTracks.remove(songIndex);
        }

        if (songIndex < trackIndex){
            trackIndex --;
        }else if(songIndex == trackIndex){
            skipAndPlay(0);
        }
    }

    /**
     * Stops playing the current audio track (if any) and starts playing a new AudioTrack.
     * @param audioTrack The new audio track to start playing
     */
    public void playNow(AudioTrack audioTrack) {
        if (audioTrack != null){
            player.startTrack(audioTrack.makeClone(), false);
        }else {
            player.startTrack(null, false);
        }
    }

    public void playNowAndInsert(AudioTrack audioTrack) {
        audioTracks.add(trackIndex, audioTrack);
        playNow(audioTrack);
    }

    /**
     Plays a song at a specific song index.
     The song index is between [0, size - 1]

     @param songIndex The song index to start playing.
     */
    public void play(int songIndex) {
        playNow(audioTracks.get(songIndex));
    }

    /**
     * Plays a song at the current index + the amount specified.
     * If the loop mode is Loop_Queue, a few extra rules apply:
     * - If the end of the queue is reached (index = queue_size), then the new index is 0.
     * - If the start of the queue is reached (index = -1), then the new index is queue_size - 1
     *
     * @param amount The amount to skip, can be negative and zero.
     */
    public void skipAndPlay(int amount) {
        int newTrackIndex = trackIndex + amount;

        if(loopMode == LoopMode.QUEUE && newTrackIndex < 0){
            newTrackIndex = audioTracks.size() - 1;
        }else if(loopMode == LoopMode.QUEUE && newTrackIndex >= audioTracks.size()){
            newTrackIndex = 0;
        }

        trackIndex = newTrackIndex;
        if(newTrackIndex >= 0 && newTrackIndex < audioTracks.size()){
            play(trackIndex);
        }else{
            playNow(null);
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        Logger.log("Track Start: " + Utils.formatAudioTrack(track, false), Logger.LogType.INFO);
        failedAttempts = 0;

        if (loopMode == LoopMode.QUEUE){
            commandsChannel.sendMessage("**Now Currently Playing:** " + Utils.formatAudioTrack(track, false)).queue();
        }
        musicPanel.update();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        Logger.log("Track End: " + Utils.formatAudioTrack(track, false) + " (Reason: "+endReason.toString() + "),", Logger.LogType.INFO);



        if(endReason == AudioTrackEndReason.LOAD_FAILED && failedAttempts < 3){
            // We really need to play this track, no skipping!
            playNow(track);
            commandsChannel.sendMessage("Loading FAILED, retrying...").queue();
            failedAttempts++;
        }else if (endReason.mayStartNext){
            if (failedAttempts == 3) {
                commandsChannel.sendMessage("Loading FAILED three times, this is MOST likely due to the song being age restricted on youtube. The song will be removed from the queue.").queue();
                removeSongFromQueue(trackIndex);
                sendQueue();
                failedAttempts = 0;
                return;
            }

            switch (loopMode){
                case QUEUE:
                case OFF:
                    skipAndPlay(1);
                    break;

                case SONG: playNow(track.makeClone()); break;
            }
        }
        musicPanel.update();
    }
}