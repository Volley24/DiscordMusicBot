//package max;
//
//import max.waouh.DiscordStorage;
//import max.waouh.UserData;
//import max.waouh.Utils;
//import net.dv8tion.jda.api.JDA;
//import net.dv8tion.jda.api.entities.User;
//import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
//import net.dv8tion.jda.api.hooks.ListenerAdapter;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.*;
//
//
//public class WaouhBot extends ListenerAdapter {
//    private final List<String> words = new ArrayList<>(Arrays.asList("waouh", "mauvais", "nion"));
//    private final DiscordStorage discordStorage;
//    private final HashMap<String, UserData> userRecord;
//
//    // waouh
//    public WaouhBot(JDA jda) {
//        jda.addEventListener(this);
//
//        discordStorage = new DiscordStorage(jda, "985364783572324402");
//        discordStorage.findMessage();
//
//        this.userRecord = Utils.parse(discordStorage.getMessage());
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
//        UserData userData = this.userRecord.get(id);
//
//        if (event.getChannel().getId().equals("981345505126801450")){
//            for (String word : words){
//                if (message.contains(word)){
//                    int amount = userData.words.getOrDefault(word, 0);
//                    userData.words.put(word, amount + 1);
//                }
//            }
//
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
//
//                case "leaderboard" -> {
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
//}
