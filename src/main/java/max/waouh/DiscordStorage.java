package max.waouh;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class DiscordStorage {
    private final JDA jda;

    private final TextChannel textChannel;
    private Message message;

    public DiscordStorage(JDA jda, String channelId){
        this.jda = jda;

        this.textChannel = jda.getTextChannelById(channelId);

        if(textChannel == null){
            throw new IllegalArgumentException("Text channel doesn't exist.");
        }
    }

    public void findMessage(){
        List<Message> messageList = textChannel.getHistory().retrievePast(2).complete();

        if(messageList.size() == 0){
            this.message = textChannel.sendMessage("{ }").complete();
            System.out.println("[SUCCESS] No message present in guild, sent one.");
        }else if(messageList.size() == 1){

            if(messageList.get(0).getAuthor().getId().equals(jda.getSelfUser().getId())){
                this.message = messageList.get(0);
                System.out.println("[SUCCESS] Message present in guild sent by bot.");
            }else{
                throw new IllegalStateException("No messages of another user must be present in this channel.");
            }
        }else{
            throw new IllegalStateException("No messages must be present in this channel.");
        }

    }

    public String getMessage(){
        return message.getContentRaw();
    }

    public void setMessage(String contents, boolean blockThread){
        if(blockThread){
            message.editMessage(contents).complete();
        }else{
            message.editMessage(contents).queue();
        }
    }
}
