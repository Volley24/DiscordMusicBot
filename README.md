# DiscordMusicBot

A Discord Music Bot built using Java aswell as JDA, and LavaPlayer.

This is a music discord bot that uses the Java Discord API (JDA) and a discord music player API (LavaPlayer) that uses slash commands and buttons in discord to add, play, loop and remove songs.

This music bot holds `AudioTrack` (A class provided by LavaPlayer) inside an arraylist aswell as an integer describing the index of the currently playing song.

## Key Features:
- Uses new features from discord, such as slash commands and interactable buttons
- Shows a panel with the currently playing song, buttons to interact with, and current songs inside the queue when execting any command. [Picture of embed](/imgs/discord_bot_embed.png)
- Bunch of commands available for interacting with the player [Picture of available commands](/imgs/available_commands.png) [Commands showcase](/imgs/command_showcase.png)
   - /add [query]- Takes the query paramater from the user, and uses the LavaPlayer library to search youtube for a match (returning a list of audio tracks matching that query), and adds the first match in that list. This new audio track is added to a queue.
   - /remove - Removes a song from the spesified song position (song position = song index + 1)
   - /queue - Displays the interactive panel.
   - /vol [num] - Set the volume of the player, again using the lavaplayer library to control the playback volume.
- Able to use buttons to pause/play, go back, skip, and change the loop mode of the player
  - Pause/Play pauses or resumes the bot.
  - Prev stops playing the current song and goes to the previous one, if available
  - Skip stops playing the current song and goes to the next one, if available
  - Loop Mode changes the bot's loop mode, it currently has three: NO_LOOP, LOOP_SONG, and LOOP_QUEUE. These are self-explanatory.
- Able to view pages of the queue via a drop-down menu 'Select a page number'