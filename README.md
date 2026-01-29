# Movie Guess Bot 🎬

A Discord bot that lets users guess movies from GIFs! The bot uses minimal Discord intents and permissions for verification compliance.

Invite link: https://discord.com/oauth2/authorize?client_id=1466485885074800854&permissions=277025409024&integration_type=0&scope=bot

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

- `/start` - Start a new movie guessing round with a GIF
- `/guess <title>` - Guess the movie title (⚠️ ONE ATTEMPT ONLY!)
- `/leaderboard` - View server and member leaderboards
- `/skip` - Skip the current movie, reveal the answer, and start a new round

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

### Gateway Intents
- ✅ **GUILDS** - Access to guild information (guild ID, guild name)
- ✅ **GUILD_MEMBERS** - Access to member information (member ID, member nickname)

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

```bash
java -jar build/libs/movie-guess-bot-1.0.0.jar
```

You should see:
```
Database initialized successfully!
Movie Guess Bot is ready!
```

### Step 4: Play!

In your Discord server, try these commands:

1. `/start` - Start a movie guessing game
2. `/guess Inception` - Make your ONE guess
3. `/leaderboard` - See who's winning
4. `/skip` - Reveal the answer and get a new movie

## Project Structure

```
movie-guess-bot/
├── MovieGuessBot.java           # Main bot class and entry point
├── CommandListener.java         # Handles slash command interactions
├── DatabaseManager.java         # SQLite database operations
├── MovieProvider.java           # Movie data with GIF URLs
├── MovieMatcher.java            # Fuzzy matching for movie titles
├── build.gradle                 # Gradle build configuration
├── settings.gradle              # Gradle settings
├── gradlew                      # Gradle wrapper script (Unix)
├── gradlew.bat                  # Gradle wrapper script (Windows)
├── gradle/wrapper/              # Gradle wrapper files
├── README.md                    # This file
└── movieguess.db               # SQLite database (created on first run)
```

## How It Works

### One-Guess Rule

Each user can only guess once per movie:
1. User makes a guess with `/guess`
2. Bot checks if they've already guessed for this movie
3. If yes: they're told to wait
4. If no: their guess is recorded and checked
5. Correct guess: they score, round ends
6. Wrong guess: answer revealed, round ends, no score

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

### Database Schema

**active_movies**
- `guild_id` (PRIMARY KEY): Discord server ID
- `movie_title`: The movie to guess
- `gif_url`: GIF URL for the movie
- `created_at`: Timestamp

**active_guesses** (NEW!)
- `guild_id, member_id` (COMPOSITE PRIMARY KEY)
- `guessed_at`: When the member guessed

**guild_leaderboard**
- `guild_id` (PRIMARY KEY): Discord server ID
- `guild_name`: Server name
- `correct_guesses`: Total correct guesses

**member_leaderboard**
- `guild_id, member_id` (COMPOSITE PRIMARY KEY)
- `member_nickname`: Member's display name
- `correct_guesses`: Total correct guesses

## Gradle Tasks

```bash
# Build the project
./gradlew build

# Clean build artifacts
./gradlew clean

# Create a fat JAR with all dependencies
./gradlew shadowJar

# See all available tasks
./gradlew tasks
```

## Customization

### Adding More Movies

Edit `MovieProvider.java` and add entries to the `MOVIES` array:

```java
new Movie("Your Movie Title", "https://media.giphy.com/media/YOUR_GIF_ID/giphy.gif")
```

### Adjusting Match Sensitivity

Edit `MovieMatcher.java` to change the matching threshold:

```java
return similarity >= 0.8; // Change 0.8 (80%) to your preferred threshold
```

## Troubleshooting

### "Bot doesn't respond to commands"
- Ensure slash commands are registered (wait up to 1 hour for global commands)
- Check bot has proper permissions in the server
- Verify SERVER MEMBERS INTENT is enabled in Discord Developer Portal

### "Gradle command not found"
- Use `./gradlew` (Linux/Mac) or `gradlew.bat` (Windows) instead
- The Gradle Wrapper downloads Gradle automatically on first run

### "Permission denied: ./gradlew"
- Run: `chmod +x gradlew`

### Database errors
- Ensure the bot has write permissions in its directory
- Delete `movieguess.db` to reset the database (will lose all data)

### GIFs not displaying
- Check if Discord can access the Giphy URLs
- Some GIF URLs may expire; update them in `MovieProvider.java`

## Why Gradle?

- ✅ **No installation required**: Gradle Wrapper downloads everything automatically
- ✅ **Cross-platform**: Works on Windows, Mac, Linux
- ✅ **Modern**: Industry-standard build tool
- ✅ **Fast**: Incremental builds and caching
- ✅ **Easy dependencies**: Automatic dependency management

## Privacy & Data Collection

The bot only collects and stores:
- Guild ID and name
- Member ID and nickname
- Game statistics (correct guesses count)
- Who has guessed for the current movie

No message content or other user data is collected or stored.

## License

This project is provided as-is for educational purposes.

## Support

For issues or questions, please create an issue in the project repository.

---

Made with ❤️ for movie lovers and Discord communities! 🎬🍿
