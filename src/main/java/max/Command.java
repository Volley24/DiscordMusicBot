package max;

import max.music.MusicBot;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

public class Command extends CommandData {
    // Access needed to execute command
    // Used only for Music Bot.
    // Use CommandAccessorType.ANYONE for commands not music related/that anyone can execute
    public MusicBot.CommandAccessorType commandAccessorType;

    public Command(@NotNull String name) {
        super(name, "");

        this.commandAccessorType = MusicBot.CommandAccessorType.ANYONE;
    }

    public Command setAccess(@NotNull MusicBot.CommandAccessorType commandAccessorType){
        this.commandAccessorType = commandAccessorType;
        return this;
    }
}
