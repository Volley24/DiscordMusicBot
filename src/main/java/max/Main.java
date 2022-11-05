package max;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import javax.security.auth.login.LoginException;

public class Main extends ListenerAdapter {
    // CONSTANTS
    public static final boolean IS_PRODUCTION = true;
    public static final String VERSION = "v.1.2.1";

    public static void main(String[] args) throws LoginException, InterruptedException {
        BotLauncher botLauncher = BotLauncher.BUILD(Secrets.TOKEN);
        botLauncher.start();
    }
}
