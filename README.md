# Guess The Movie Bot 🎬

A Discord bot that lets users guess movies from GIFs! The bot uses minimal Discord intents and permissions for verification compliance.

Invite link: https://discord.com/oauth2/authorize?client_id=1466485885074800854&permissions=277025409024&integration_type=0&scope=bot

ToS and Privacy policy: https://kristiyandinev.github.io/Discord-Movie-Bot-Static-Webpage/

## Features

- 🎮 **Movie Guessing Game**: Bot shows a GIF from a random popular movie
- 🎯 **One Attempt Only**: Each user gets ONE guess per movie - guess wisely!
- 🤖 **Smart Matching**: Fuzzy matching algorithm that accepts partial titles, handles typos, ignores special characters and case
- 🔴 **Answer Revealed**: Wrong guesses reveal the answer immediately and end the round
- 📊 **Dual Leaderboards**:
    - Server leaderboard (guilds with most correct guesses)
    - Member leaderboard (top players per server)
- 💾 **SQLite Database**: Persistent storage of active games and leaderboards
- ✅ **Discord Verified**: Uses only required intents (GUILDS, GUILD_MEMBERS) for bot verification
- 🏗️ **Gradle Build**: Easy setup with Gradle Wrapper (no Gradle installation required!)

## Commands

- `/start [movie/anime] [total_rounds: int] [high_stakes: true / false] [locked: true / false]` - Start a new movie guessing round with a GIF
- `/guess <title>` - Guess the movie title 
- `/leaderboard` - View server and member leaderboards
- `/skip` - Skip the current movie, reveal the answer and try again
- `/help` - Displays the help command
- `/updates` - See what's new in the latest version

## Game Rules

1. Use `/start` to begin a new round - a GIF will be shown
2. Each member gets **ONE guess only** per movie
3. If you guess correctly ✅:
    - Your score increases
    - The round ends and a new movie can be started
4. If you guess incorrectly ❌:
    - The answer is revealed to everyone
    - The round ends immediately
    - No score is awarded
5. Use `/skip` to give up and see the answer

## Required Permissions & Intents

### Bot Permissions
- ✅ **Send Messages** - To send GIFs and responses
- ✅ **Embed Links** - To send rich embeds
- ✅ **Use Slash Commands** - For command interactions

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Discord bot token
- **No Gradle installation required!** (Uses Gradle Wrapper)

### Step 1: Create Discord Bot

1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Create a new application
3. Go to the "Bot" section and create a bot
4. Enable the following **Privileged Gateway Intents**:
    - ✅ SERVER MEMBERS INTENT
5. Copy the bot token
6. Go to OAuth2 > URL Generator
7. Select scopes: `bot`, `applications.commands`
8. Select permissions: `Send Messages`, `Embed Links`, `Use Slash Commands`
9. Use the generated URL to invite the bot to your server

### Step 2: Build the Project

#### Using Gradle Wrapper (Recommended - No Gradle installation needed!)

**Linux/Mac:**
```bash
# Make gradlew executable (first time only)
chmod +x gradlew

# Build the project
./gradlew build

# The JAR will be in build/libs/movie-guess-bot-1.0.0.jar
```

**Windows:**
```cmd
# Build the project
gradlew.bat build

# The JAR will be in build\libs\movie-guess-bot-1.0.0.jar
```

#### Using Installed Gradle (If you have Gradle installed)
```bash
gradle build
```

### Step 3: Run the Bot

The bot uses these ENV variables in order to work:

- `POSTGRES_DB_HOST` - The PostgresSQL database HOST Address
- `POSTGRES_DB_PORT` - The PostgresSQL database Port
- `POSTGRES_DB_USERNAME` - The PostgresSQL user to connect as
- `POSTGRES_DB_PASSWORD` - The PostgresSQL user's password for authentication.
- `REPORT_GUILD_ID` - The guild id where the report channel is
- `REPORT_CHANNEL_ID` - The text channel id where to send the reports

The following are **optional** and they default to some value if they are not set:

- `HIKARICP_MAX_POOL_SIZE` = 10
- `HIKARICP_MINIMUM_IDLE` = 2
- `HIKARICP_CONNECTION_TIMEOUT`= 30000 (ms), which is 30 seconds
- `HIKARICP_IDLE_TIMEOUT` = 60000 (ms), which is 1 min.
- `HIKARICP_MAX_LIFETIME` = 1800000 (ms), which is 30 mins
- `HIBERNATE_HDM2DDL_AUTO` = update
- `HIBERNATE_SHOW_SQL` = false
- `HIBERNATE_JDBC_BATCH_SIZE` = 50
- `HIBERNATE_ORDER_INSERTS` = true
- `HIBERNATE_ORDER_UPDATES` = true

```bash
docker build --build-args BOT_VERSION=1.0.0 -t discordmoviebot:1.0.0 .
docker compose up -d
```

Then you will get an error, which says that you can't connect.
Go to your postgres database and run all the commands from `init.sql`
Then restart only the discord bot container and you should be good to go.

### Step 4: Play!

In your Discord server, try these commands:

1. `/start` - Start a movie guessing game
2. `/guess Inception` - Make your ONE guess
3. `/leaderboard` - See who's winning
4. `/skip` - Reveal the answer and get a new movie

## How It Works

### Fuzzy Matching Algorithm

The bot uses intelligent matching to accept various forms of movie titles:

- **Case insensitive**: "the matrix" matches "The Matrix"
- **Special characters**: "spiderman" matches "Spider-Man"
- **Partial titles**: "dark knight" matches "The Dark Knight"
- **Typos**: Uses Levenshtein distance (80% similarity threshold)
- **Article handling**: Ignores "the", "a", "an" in comparisons
- **Word matching**: If guess contains all significant words from title, it's accepted

Examples:
- Movie: "The Lord of the Rings"
- ✅ Accepts: "lord of the rings", "lord rings", "Lord of Rings"
- ❌ Rejects: "harry potter", "star wars"

## License

This project is provided as-is for educational purposes.

## Support

For issues or questions, please create an issue in the project repository.

---

Made with ❤️ for movie lovers and Discord communities! 🎬🍿
