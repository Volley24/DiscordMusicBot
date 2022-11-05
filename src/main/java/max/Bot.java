package max;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Bot extends ListenerAdapter {
    public Bot(JDA jda) {
        jda.addEventListener(this);
    }
}
