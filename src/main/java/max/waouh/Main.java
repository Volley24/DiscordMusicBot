//import max.waouh.Utils;
//import max.waouh.DiscordStorage;
//import max.waouh.UserData;
//import net.dv8tion.jda.api.JDA;
//import net.dv8tion.jda.api.JDABuilder;
//import net.dv8tion.jda.api.entities.User;
//import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
//import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
//import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
//import net.dv8tion.jda.api.hooks.ListenerAdapter;
//import net.dv8tion.jda.api.utils.ChunkingFilter;
//import net.dv8tion.jda.api.utils.cache.CacheFlag;
//import org.jetbrains.annotations.NotNull;
//
//import javax.security.auth.login.LoginException;
//import java.util.*;
//
//public class Main extends ListenerAdapter {
//    public static String FILE_PATH = "C:\\Users\\Max\\Desktop\\Coding\\SkullEmoji\\src\\main\\resources\\score.json";
//    public String prevContents = "";
//
//    public static void main(String[] args) throws LoginException, InterruptedException {
//        JDABuilder builder = JDABuilder.createDefault(Secrets.TOKEN);
//
//        builder.setChunkingFilter(ChunkingFilter.ALL);
//        builder.enableCache(CacheFlag.VOICE_STATE);
//
//        new Main(builder.build().awaitReady());
//    }
//
//    private final JDA jda;
//    private final HashMap<String, UserData> userRecord;
//    private final DiscordStorage discordStorage;
//
//    private String eventId = null;
//
//    public Main(JDA jda){
//        this.jda = jda;
//        this.jda.addEventListener(this);
//
//
//        discordStorage = new DiscordStorage(jda, "985364783572324402");
//        discordStorage.findMessage();
//
//        this.userRecord = Utils.parse(discordStorage.getMessage());
//
//
//        Runtime.getRuntime().addShutdownHook(new Thread(this::saveToFile));
//        Timer timer = new Timer("Timer");
//
//        long delay = 10000L;
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                saveToFile();
//            }
//        }, delay, delay);
//    }
//
//    public void saveToFile(){
//
//        String export = Utils.export(userRecord);
//        if (!export.equals(prevContents)){
//            discordStorage.setMessage(export, false);
//            prevContents = export;
//            System.out.println("Saving to file...");
//        }
//
//    }
//
//    public UserData get(User user){
//        UserData userData = userRecord.get(user.getId());
//
//        if(userData == null){
//            userData = new UserData(user.getName(), user.getId(), 0, 0);
//            userRecord.put(user.getId(), userData);
//        }
//        return userData;
//    }
//
//    public void increment(User user, int amount){
//        UserData userData = get(user);
//        userData.points += amount;
//
//        if (userData.points < -300){
//            userData.points = -300;
//        }
//    }
//    @Override
//    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
//        if (event.getMember() == null)
//            return;
//
////        if (event.getChannel().getId().equals("981345505126801450") && event.getMessageId().equals(eventId)){
////            System.out.println("On that event u reacted with: " + event.getReactionEmote().getEmoji());
////        }
//    }
//
//    @Override
//    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
//        if (event.getAuthor().isBot() || event.getMember() == null)
//            return;
//
//        String message = event.getMessage().getContentRaw();
//
//        if (event.getChannel().getId().equals("981345505126801450")){
//            if (!message.equals("\uD83D\uDC80")){
//                increment(event.getAuthor(), -150);
//                event.getMessage().reply("u think editing your message will get past me? -150 points for being a massive noin.").queue();
//            }
//        }
//    }
//
//    @Override
//    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
//        if (event.getAuthor().isBot() || event.getMember() == null)
//            return;
//
//        User user = event.getAuthor();
//        String message = event.getMessage().getContentRaw();
//        String id = event.getMember().getId();
//
//        if (event.getChannel().getId().equals("981345505126801450")){
//            if (message.equals("\uD83D\uDC80")){
//                if(!event.getMessage().getAttachments().isEmpty()){
//                    increment(user, -100);
//                    event.getMessage().reply("nice try lmao, attachments aren't allowed. -100 points.").queue();
//                }else{
//                    increment(user, 1);
//
////                    if (Utils.chanceHappens(1, 2)){
////                        // An event occurs!
////                        event.getChannel().sendMessage(":skull-crossbones: attacks! React with :skull: to attack (4s)").queue((msg) -> {
////                            eventId = msg.getId();
////
////                            Timer timer = new Timer("Timer");
////
////                            int delay = 4000;
////                            timer.schedule(new TimerTask() {
////                                @Override
////                                public void run() {
////                                    msg.delete().queue();
////                                }
////                            }, delay);
////                        });
////                    }
//                }
//            }else{
//                increment(user, -100);
//                event.getMessage().reply("-100 points u nion").queue();
//            }
//        }else if(event.getChannel().getId().equals("897616896566898698")){
//            String[] split = message.substring("*".length()).split(" ");
//
//            if (!message.startsWith("*"))
//                return;
//
//
//            switch (split[0]) {
//                case "pts" -> {
//                    UserData userData = get(user);
//
//                    int points = userData.points;
//                    int nion = userData.legendaryNionTokens;
//                    String reply = "You have " + points + " points";
//                    if (nion != 0) {
//                        reply += "\n(You also have " + nion + " legendary nion tokens!)";
//                    }
//
//                    if (points < 0) {
//                        reply += "\n**Imagine having negative points :skull:**";
//                    }
//                    event.getMessage().reply(reply).queue();
//                }
//                case "nion" -> event.getMessage().reply("nion").queue();
//                case "top" -> {
//                    ArrayList<UserData> list = new ArrayList<>();
//                    for (String key : userRecord.keySet()) {
//                        UserData userData = userRecord.get(key);
//
//                        list.add(userData);
//                    }
//                    list.sort(Collections.reverseOrder(Comparator.comparingInt(userData -> userData.points)));
//
//                    StringBuilder send = new StringBuilder("**Here are the leaderboards for most points:**\n\n");
//
//                    int pos = 1;
//                    int mostPoints = list.get(0).points;
//                    for (UserData userData : list) {
//                        if(mostPoints != userData.points){
//                            pos += 1;
//                            mostPoints = userData.points;
//                        }
//
//                        send.append(String.format("**#%d:** %s - [%d points]%s", pos, userData.name, userData.points, userData.legendaryNionTokens != 0 ? " [" + userData.legendaryNionTokens + " legendary nion tokens]" : "")).append("\n");
//
//                    }
//                    event.getChannel().sendMessage(send).queue();
//                }
//                case "give" -> {
//                    if (!user.getId().equals("880428502371938316")){
//                        event.getMessage().reply("How about no lmao").queue();
//                        return;
//                    }
//
//                    String newId = split[1];
//                    int amount = Integer.parseInt(split[2]);
//
//                    event.getJDA().retrieveUserById(newId).queue((newUser) -> {
//                        UserData userData = get(newUser);
//
//                        userData.points += amount;
//                        event.getMessage().replyFormat("Successfully gave %s %d points.", newUser.getName(), amount).queue();
//                    });
//
//                }
//            }
//        }
//    }
//}
