package max;

import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import max.music.MusicBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;

public class BotLauncher {
    private final JDA jda;

    public BotLauncher (@NotNull JDA jda) {
        this.jda = jda;
    }

    public static BotLauncher BUILD(String token) throws LoginException, InterruptedException {
        JDABuilder builder = JDABuilder.createDefault(token);

        builder.setChunkingFilter(ChunkingFilter.ALL);
        builder.enableCache(CacheFlag.VOICE_STATE);
        builder.setAudioSendFactory(new NativeAudioSendFactory());

        return new BotLauncher(builder.build().awaitReady());
    }

    public void start() {
        new MusicBot(jda);
        jda.getPresence().setActivity(Activity.playing("Music"));

//        for (Command command : commands) {
//            jda.upsertCommand(command).queue();
//        }

        jda.upsertCommand("help", "List all the available commands for the Bon Music Bot.")
                .queue();

        jda.upsertCommand("queue", "Opens the music bot panel.")
                .queue();

        jda.upsertCommand("skip", "Plays the next song.")
                .queue();
        jda.upsertCommand("prev", "Plays the previous song.")
                .queue();

        jda.upsertCommand("add", "Adds a song to the queue.")
                .addOption(OptionType.STRING, "song-name", "The song name or playlist to search for.", true)
                .queue();
        jda.upsertCommand("playnow", "Stops the current song and plays another specified song. Will re-play stopped song afterwards.")
                .addOption(OptionType.STRING, "song-name", "The song name to play now", true)
                .queue();

        jda.upsertCommand("remove", "Remove a specified song from the queue.")
                .addOption(OptionType.INTEGER, "song-position", "The position of the song to erase.", true)
                .queue();
        jda.upsertCommand("clear", "Clears the entire queue.")
                .queue();

        jda.upsertCommand("volume", "Change the playback volume of the music bot.")
                .addOption(OptionType.INTEGER, "volume", "The new volume of the music bot. Must be between 0 and 100.", true)
                .queue();

        jda.upsertCommand("join", "Makes me join the current voice chat you are in.")
                .queue();
        jda.upsertCommand("dc", "Makes me leave the current voice channel I am in.")
                .queue();
    }
}
