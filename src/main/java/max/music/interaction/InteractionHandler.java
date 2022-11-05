package max.music.interaction;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Message;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.util.ArrayList;

public abstract class InteractionHandler extends ListenerAdapter {
    private final GuildMessageChannel guildChannel;
    protected Message currentMessage;

    public InteractionHandler(GuildMessageChannel guildChannel){
        this.guildChannel = guildChannel;
    }

    public void sendNew(){
        if (currentMessage != null)
            currentMessage.delete().queue();
        guildChannel.sendMessageEmbeds(getEmbedBuilder().build()).setActionRows(getActionRows()).queue(message -> currentMessage = message);
    }

    public void update(){
        if(currentMessage == null)
            return;

        currentMessage.editMessageEmbeds(getEmbedBuilder().build()).setActionRows(getActionRows()).queue();
    }

    public void delete(){
        if (currentMessage != null)
            currentMessage.delete().queue();

        currentMessage = null;
    }

    protected abstract EmbedBuilder getEmbedBuilder();
    protected abstract ArrayList<ActionRow> getActionRows();

    protected boolean messageIsActiveInteraction(Message message){
        return currentMessage != null && message.getId().equals(currentMessage.getId());
    }
}
