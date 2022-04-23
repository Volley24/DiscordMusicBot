package max.interaction;


import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import max.TrackScheduler;
import max.utils.Logger;
import max.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.ArrayList;

public class MusicPanel extends InteractionHandler{
    private final TrackScheduler trackScheduler;
    private int page = 1;

    public MusicPanel(TrackScheduler trackScheduler){
        super(trackScheduler.getCommandsChannel());
        this.trackScheduler = trackScheduler;
        trackScheduler.getCommandsChannel().getJDA().addEventListener(this);
    }

    @Override
    public EmbedBuilder getEmbedBuilder() {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Bon Music Bot\u2122 - Panel");
        embedBuilder.setColor(Color.GREEN);

        StringBuilder stringBuilder = new StringBuilder();

        addLabel(stringBuilder, "Currently playing",  Utils.formatAudioTrack(trackScheduler.getPlayingSong(), true));
        addBlank(stringBuilder, 1);

        addLabel(stringBuilder, "Queue", "(Showing page 1 of " + trackScheduler.getPageAmount() + ")");

        addText(stringBuilder, trackScheduler.getSongsOnPage(page));

        int remainingSongs = trackScheduler.getRemainingSongs(page);
        if (remainingSongs != 0){
            addText(stringBuilder, "**...and " + remainingSongs + " more**");
        }

        addBlank(stringBuilder, 1);

        String queueEmoji = "";
        TrackScheduler.LoopMode loopMode = trackScheduler.getLoopMode();
        switch (loopMode){
            case OFF -> queueEmoji = "\u27A1";
            case SONG -> queueEmoji = "\uD83D\uDD02";
            case QUEUE -> queueEmoji = "\uD83D\uDD01";
        }

        String volumeEmoji;
        int volume = trackScheduler.getPlayer().getVolume();
        if (volume == 0){
            volumeEmoji = "\uD83D\uDD07";
        }else if(volume > 0 && volume <= 50){
            volumeEmoji = "\uD83D\uDD09";
        }else{
            volumeEmoji = "\uD83D\uDD0A";
        }


        addLabel(stringBuilder, "Music Status", trackScheduler.getPlayer().isPaused() ? "Paused \u23F8": "Playing \u25B6");
        addLabel(stringBuilder, "Loop Mode", "Loop " + loopMode.getName() + " " + queueEmoji);
        addLabel(stringBuilder, "Volume", volume + "/100 "+volumeEmoji);

        if(trackScheduler.getPlayer().isPaused()){
            addBlank(stringBuilder, 1);
            addText(stringBuilder, "** == PLAYER IS PAUSED == **");
        }

        embedBuilder.setDescription(stringBuilder.toString());

        embedBuilder.setFooter("Made by Max_723");
        return embedBuilder;
    }

    @Override
    public ArrayList<ActionRow> getActionRows() {
        ArrayList<ActionRow> actionRows = new ArrayList<>();

        Button pausePlay = Button.primary("pause-play", trackScheduler.getPlayer().isPaused() ? "Play" : "Pause")
                .withEmoji(trackScheduler.getPlayer().isPaused() ? Emoji.fromMarkdown("\u25B6") : Emoji.fromMarkdown("\u23F8"));
        Button prev = Button.primary("prev", "Prev")
                .withEmoji(Emoji.fromMarkdown("\u23EE"))
                .withDisabled(trackScheduler.trackIndex == 0 && trackScheduler.getLoopMode() != TrackScheduler.LoopMode.QUEUE);
        Button skip = Button.primary("skip", "Skip")
                .withEmoji(Emoji.fromMarkdown("\u23ED"))
                .withDisabled(trackScheduler.trackIndex >= trackScheduler.audioTracks.size() && trackScheduler.getLoopMode() != TrackScheduler.LoopMode.QUEUE);
        Button exit = Button.danger("exit", "Exit");


        Button noLoop = Button.primary("loop-off", "Loop Off").withEmoji(Emoji.fromMarkdown("\u27A1")).withDisabled(trackScheduler.getLoopMode() == TrackScheduler.LoopMode.OFF);
        Button loopSong = Button.primary("loop-song", "Loop Song").withEmoji(Emoji.fromMarkdown("\uD83D\uDD02")).withDisabled(trackScheduler.getLoopMode() == TrackScheduler.LoopMode.SONG);
        Button loopQueue = Button.primary("loop-queue", "Loop Queue").withEmoji(Emoji.fromMarkdown("\uD83D\uDD01")).withDisabled(trackScheduler.getLoopMode() == TrackScheduler.LoopMode.QUEUE);


        SelectionMenu.Builder selectionMenuBuilder = SelectionMenu.create("page-select").setPlaceholder("Select a page number");
        for (int i = 0; i < trackScheduler.getPageAmount(); i++){
            selectionMenuBuilder.addOption("Page "+(i + 1), String.valueOf(i + 1));
        }

        actionRows.add(ActionRow.of(pausePlay, prev, skip, exit));
        actionRows.add(ActionRow.of(noLoop, loopSong, loopQueue));
        actionRows.add(ActionRow.of(selectionMenuBuilder.build()));

        return actionRows;
    }


    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        Button button = event.getButton();
        if (!messageIsActiveInteraction(event.getMessage())){
            if (button != null && button.getId() != null && button.getId().equals("exit")){
                event.getMessage().delete().queue();
            }else {
                event.reply("Sorry, but this panel is inactive!").setEphemeral(true).queue();
            }
            return;
        }

        Logger.log("\nButton click received: " + event.getButton().getId(), Logger.LogType.INFO);

        if (button != null && button.getId() != null){
            AudioPlayer audioPlayer = trackScheduler.getPlayer();
            switch (button.getId()){
                case "pause-play" -> audioPlayer.setPaused(!audioPlayer.isPaused());

                case "skip" -> trackScheduler.skipAndPlay(1);
                case "prev" -> trackScheduler.skipAndPlay(-1);

                case "loop-off" -> trackScheduler.setLoopMode(TrackScheduler.LoopMode.OFF);
                case "loop-song" -> trackScheduler.setLoopMode(TrackScheduler.LoopMode.SONG);
                case "loop-queue" -> trackScheduler.setLoopMode(TrackScheduler.LoopMode.QUEUE);

                case "exit" -> {
                    delete();
                    return;
                }

                default -> System.out.println("Unknown button ID: " + event.getButton().getId());

            }
        }
        event.editMessageEmbeds(getEmbedBuilder().build()).setActionRows(getActionRows()).queue();
    }

    @Override
    public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
        if (!messageIsActiveInteraction(event.getMessage())){
            event.reply("Sorry, but this panel is inactive!").setEphemeral(true).queue();
            return;
        }
        SelectionMenu selectionMenu = event.getSelectionMenu();

        if (selectionMenu != null && selectionMenu.getId() != null && selectionMenu.getId().equals("page-select")){
            this.page = Integer.parseInt(event.getSelectedOptions().get(0).getValue());
            update();
        }
        event.editMessageEmbeds(getEmbedBuilder().build()).setActionRows(getActionRows()).queue();
    }


    private void addText(StringBuilder stringBuilder, String text){
        stringBuilder.append(text).append("\n");
    }

    private void addLabel(StringBuilder stringBuilder, String title, String value){
        stringBuilder.append("**").append(title).append(":** ").append(value).append("\n");
    }
    private void addBlank(StringBuilder stringBuilder, int amount){
        stringBuilder.append("\n".repeat(amount));
    }
}
