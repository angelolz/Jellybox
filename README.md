# Jellybox

A Discord Music box for your Jellyfin server. Jellybox is based off an old bot that I wrote for a college group project,
but removed all the sources (youtube, spotify, bandcamp, etc) and only has a Jellyfin source attached to it.

# Setup

**Prerequisites**: do these steps first before trying to run the bot.

## Setup Discord bot

1) Go to the [Discord Developers](https://discord.com/developers) page and click **Get Started**.
2) Click on **New Application** in the top right.
3) Type in the name of your application (it can be anything), then click **Create**.
4) Click on the **OAuth2** Section.
5) Check the **Public Client** toggle.
6) In the **OAuth2 URL Generator**, check the `bot` scope.
7) For bot permissions, enable the following permissions for the bot:
    - General Permissions
        - View Channels
    - Text Permissions
        - Send Messages
        - Send Messages in Threads
        - Embed Links
        - Read Message History
        - Use Slash Commands
    - Voice Permissions
        - Connect
        - Speak
        - Use Voice Activity
8) Copy the **Generated URL** and keep it, you will use this to invite the bot to your server.
9) Click on the **Bot** section.
10) Click on the **Reset Token**, copy and keep the **Bot Token** that is shown.
11) Check the **Message Content Intent** toggle.
12) If you want to keep your bot private, follow these steps:
    - Go to the **Installation** tab
    - Under **Install Link**, click on the dropdown and set to **None**.
    - Go to the **Bot** tab, and uncheck the **Public Bot** toggle.

## Get Jellyfin Info
1) In the Jellyfin Web Client, click on the **Profile** Icon in the top right corner.
2) Click on **Dashboard** under the Administration portion.
3) Click on **API Keys** in the Advanced tab.
4) Press the **+** button, and give your API key a name.
5) Copy and keep the API key.
6) You will also need a user ID to give to the bot. To retrieve this, go to the **Users** tab and click on the user that you want to use for the bot.
7) In the URL Bar, you should see `dashboard/users/profile?userId=....`, retrieve the ID from there.
8) YOu will also need a Library ID to give to the bot. To do this, go to the homepage and click on the library that contains all your music.
9) In the URL Bar, you should see `/music.html?topParentId=...`, retrieve the ID from there.

**Congrats**! Now you have all you need to start the bot.
Proceed below with how you want to use the bot.
## Docker (easiest)
1) Download the `docker-compose.yml` [here](https://raw.githubusercontent.com/angelolz/Jellybox/refs/heads/main/docker-compose.yml).
2) Fill in the environment variables that you've retrieved in the previous steps.
3) Run `docker compose up`.

## Not Docker
1) Download the jar file (whenever I make it available)
2) Copy the contents of `config.properties` [here](https://raw.githubusercontent.com/angelolz/Jellybox/refs/heads/main/config.properties.example).
3) Fill in the environment variables that you've retrieved in the previous steps.
4) Make sure the file is named `config.properties` exactly. It should be placed next to the jar.
5) Open a terminal and run `java -jar <name>.jar`.

If everything was setup correctly, your Discord bot should be showing as online.

## Bot Commands
The prefix of the bot is `+`. You can change this by adding a `PREFIX` environment variable or in your `config.properties` file.

- Bot Commands
  - `+help` - shows help embed
  - `+ping` - gets the latency of the bot
  - `+join` - joins the same voice channel as you
  - `+leave` - leave the currently joined voice channel
- Player Commands
  - `+play` / `+p` - Searches for a song and queues it.
    - You can queue whole albums or artists songs too with these query parameters:
        - `+play album:<album name>` to queue an album
        - `+play artist:<artist name>` to queue an artist
  - `+pause` - pauses current track
  - `+stop` - stops playback
  - `+skip` - skip currently playing song
  - `+repeat` / `+loop` - repeat the current track or queue
  - `+shuffle` - shuffles the queue
  - `+queue` / `+q` - shows all the songs in queue
  - `+nowplaying` / `+np` - displays info of the currently playing track

If no track has been played for the last 5 minutes, the bot will automatically disconnect.