package max.utilities_bot;

import max.Bot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

public class UtilitiesBot extends Bot {

    public UtilitiesBot(JDA jda) {
        super(jda);
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        Member member = event.getMember();
        String commandName = event.getName();

        if (event.getGuild() == null || member == null){
            return;
        }

        if (commandName.equals("setcolor")){

        }
    }
}
