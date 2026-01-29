
# Quick Start Guide 🚀

## Get Your Bot Running in 5 Minutes!

### Step 1: Create Discord Bot (2 minutes)

1. Go to https://discord.com/developers/applications
2. Click "New Application" and give it a name (e.g., "Movie Guess Bot")
3. Go to "Bot" section in the left sidebar
4. Click "Add Bot"
5. Under "Privileged Gateway Intents", enable:
    - ✅ **SERVER MEMBERS INTENT** (this is required!)
6. Click "Reset Token" and copy your bot token (keep it secret!)

### Step 2: Invite Bot to Your Server (1 minute)

1. In Discord Developer Portal, go to "OAuth2" > "URL Generator"
2. Select scopes:
    - ✅ `bot`
    - ✅ `applications.commands`
3. Select bot permissions:
    - ✅ `Send Messages`
    - ✅ `Embed Links`
    - ✅ `Use Slash Commands`
4. Copy the generated URL at the bottom
5. Open the URL in your browser and select your server
6. Click "Authorize"

### Step 3: Build the Bot (1 minute)

**No Gradle installation required!** The Gradle Wrapper handles everything.

#### Linux/Mac:
```bash
# Navigate to the bot directory
cd movie-guess-bot

# Make gradlew executable (first time only)
chmod +x gradlew

# Build the project
./gradlew build
```

#### Windows:
```cmd
# Navigate to the bot directory
cd movie-guess-bot

# Build the project
gradlew.bat build
```

The first build will download Gradle automatically (takes ~30 seconds).

### Step 4: Run the Bot (1 minute)

Set `DISCORD_MOVIE_BOT_TOKEN` as env variable to your discord bot token.

```bash
java -jar build/libs/movie-guess-bot-1.0.0.jar
```

You should see:
```
Database initialized successfully!
Movie Guess Bot is ready!
```

### Step 5: Play! (∞ minutes of fun)

In your Discord server, try these commands:

1. `/start` - Start a movie guessing game
2. `/guess Inception` - Make your ONE guess (choose wisely!)
3. `/leaderboard` - See who's winning
4. `/skip` - Reveal the answer and get a new movie

## 🎯 Important: One Guess Rule!

**Each user gets only ONE attempt per movie!**
- ✅ Guess correctly → Your score increases
- ❌ Guess wrong → Answer revealed, round ends, no score

Choose your guess carefully! 🤔

## Common Issues

### "Cannot find gradlew"
- Make sure you're in the bot directory
- On Linux/Mac: Run `chmod +x gradlew` first

### "Bot doesn't respond to commands"
- Wait a few minutes for Discord to register the commands
- Make sure you enabled SERVER MEMBERS INTENT in Discord Developer Portal
- Check that the bot has permissions in your server

### "Failed to start bot: Provided token is invalid"
- Double-check your bot token from Discord Developer Portal
- Make sure there are no extra spaces in the token

### Commands don't show up
- Global commands can take up to 1 hour to appear
- Try kicking and re-inviting the bot
- Restart Discord

### Gradle takes a long time on first build
- This is normal! The Gradle Wrapper downloads Gradle on first run
- Subsequent builds will be much faster

## Gradle Commands

```bash
# Build the bot
./gradlew build          # Linux/Mac
gradlew.bat build        # Windows

# Clean and rebuild
./gradlew clean build

# Run directly (without creating JAR)
./gradlew run --args="YOUR_BOT_TOKEN"

# See all tasks
./gradlew tasks
```

## Need Help?

Check the full README.md for detailed documentation!

## Security Note ⚠️

**NEVER share your bot token publicly!** If you accidentally expose it:
1. Go to Discord Developer Portal
2. Go to your application > Bot
3. Click "Reset Token" immediately
4. Update your bot with the new token

## What Makes This Different?

Unlike other movie bots:
- 🎯 **One guess only** - makes it more challenging and exciting!
- ❌ **Wrong answers revealed** - everyone learns when someone guesses wrong
- 📊 **Fair scoring** - only correct first guesses count
- 🏗️ **Gradle** - modern, no installation needed

---

Happy movie guessing! 🎬🍿