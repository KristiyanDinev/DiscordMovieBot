# Docker: build and run

Build the image from the repository root. You can override the bot version with `BOT_VERSION` or specify an exact JAR filename with `JAR_NAME`.

Default (uses `BOT_VERSION=1.0.0`):

```bash
docker build -t discordmoviebot:latest .
```

Specify a version (example `1.2.3`):

```bash
docker build --build-arg BOT_VERSION=1.2.3 -t discordmoviebot:1.2.3 .
```

Or pass an explicit jar filename:

```bash
docker build --build-arg JAR_NAME=DiscordMovieBotShadowJar-1.2.3.jar -t discordmoviebot:1.2.3 .
```

Run the container (provide your bot token as an env var):

```bash
docker run -d --name discordmoviebot -e DISCORD_MOVIE_BOT_TOKEN="$DISCORD_MOVIE_BOT_TOKEN" discordmoviebot:1.2.3
```

Notes:
- The image includes only the requested shadow JAR from `build/libs/`.
- Make sure the jar exists at `build/libs/DiscordMovieBotShadowJar-<version>.jar` before building the image.
