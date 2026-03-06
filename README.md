# Guess The Movie Bot 🎬

A Discord bot that lets users guess movies from GIFs! The bot uses minimal Discord intents and permissions for verification compliance.

Invite link: https://discord.com/oauth2/authorize?client_id=1466485885074800854&permissions=277025409024&integration_type=0&scope=bot

ToS and Privacy policy: https://kristiyandinev.github.io/Discord-Movie-Bot-Static-Webpage/

- https://top.gg/bot/1466485885074800854

# 🎬 Guess The Movie

**Guess The Movie** is the cinematic trivia for Discord. Test your skills against a large library of scenes, compete with friends, and climb the global rankings.

---

## 🍿 Features

* **Massive Library:** Over 100+ Movies and 100+ Anime Series, hand-picked for quality.
* **Smart Matching:** Our fuzzy-logic system recognizes your answer even if you have a slight typo.
* **High Stakes:** Think you're a pro? Enable `high_stakes` mode for a more difficult challenge.
* **Solo vs. Social:** Play with the whole server in **Community Mode** or lock the game to yourself with **Solo Mode**.

---

## 🎮 How to Play

1.  **Start a Game:** Use `/start` to fire up the projector.
2.  **Analyze the Scene:** A high-quality image from a movie or anime will be displayed.
3.  **Take Your Shot:** Use `/guess <title>` to submit your answer. You only get **one chance**!

---

## 🛠️ Commands
`/start`  • Start a new round (Optional: `solo`, `high_stakes`).
`/guess`  • Submit your movie or anime title.
`/skip`  • Give up, reveal the answer, and start fresh.
`/leaderboard`  • View global and local rankings.
`/updates`  • See the latest movies and features added.
`/report` • Report an inaccurate image or title.
---
## 🏆 Competitive Play

Climb the **Member Leaderboard** to become the top critic in your server, or work together to push your guild to the top of the **Global Server Leaderboard**. Top voters and players are rewarded with special ranks!

> **Popcorn ready? Good luck!**

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
docker build --build-arg BOT_VERSION=1.0.0 -t discordmoviebot:1.0.0 .
docker compose up -d
```

Or to upload it to your own docker registry
```bash
docker build -t <registry-address>:5000/moviebot:1.0.0 .

# or if you have this image already as a local image
# docker tag moviebot:2.0.3 <registry-address>:5000/moviebot:1.0.0

docker login <registry-address>:5000
docker push <registry-address>:5000/moviebot:2.0.3
```
Then you will get an error, which says that you can't connect.
Go
 to your postgres database and run all the commands from `init.sql`
Then restart only the discord bot container and you should be good to go.

### Step 4: Play!

In your Discord server, try these commands:

1. `/start` - Start a movie guessing game
2. `/guess Inception` - Make your ONE guess
3. `/leaderboard` - See who's winning
4. `/leaderboard daly` - See the daily guesses
5. `/skip` - Reveal the answer and get a new movie
6. `/ping` - Displays the bot's latency

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

## Visualise Bot's Usage

In your pgAdmin dashboard. Got to **File -> Preferences -> Graphs -> Display**
and sure you have enabled it.


Then start a session and execute the queries you like:

The date follows this pattern: **YYYY-MM-DD**

### Get the usage of a single command during that day

- Make sure to replace `/start` with whatever command you like

```sql
WITH TargetDate AS (
    SELECT '2026-03-20'::date AS d -- Input your date here
),
Hours AS (
    -- Generates all 24 hours for the chosen day
    SELECT generate_series((SELECT d FROM TargetDate)::timestamp, 
                           (SELECT d FROM TargetDate)::timestamp + interval '23 hours', 
                           interval '1 hour') AS hour
)
SELECT 
    h.hour,
    'start_command' AS activity_source,
    COUNT(b.id) AS usage_count
FROM Hours h
LEFT JOIN bot_usage b ON date_trunc('hour', b.used_at) = h.hour 
                      AND b.command = '/start'
GROUP BY 1
ORDER BY h.hour ASC;
```


## Displays which guilds have been the most active during that day


```sql
WITH Target AS (
    SELECT '2026-03-04'::date AS d -- Change date here
),
Top10 AS (
    SELECT guildid 
    FROM bot_usage, Target
    WHERE used_at::date = d
    GROUP BY guildid 
    ORDER BY COUNT(*) DESC 
    LIMIT 10
),
OtherStats AS (
    SELECT COUNT(DISTINCT guildid) as total_others
    FROM bot_usage, Target
    WHERE used_at::date = d
    AND guildid NOT IN (SELECT guildid FROM Top10)
),
LabeledData AS (
    -- Pre-label everything so GROUP BY doesn't complain about guildid
    SELECT 
        date_trunc('hour', used_at) AS hour,
        CASE 
            WHEN guildid IN (SELECT guildid FROM Top10) THEN guildname
            ELSE 'Others'
        END AS activity_source
    FROM bot_usage, Target
    WHERE used_at::date = d
)
SELECT 
    hour,
    CASE 
        WHEN activity_source = 'Others' 
        THEN 'Others (' || (SELECT total_others FROM OtherStats) || ' guilds)'
        ELSE activity_source 
    END AS activity_source,
    CASE 
        WHEN activity_source = 'Others' 
        THEN ROUND(COUNT(*)::numeric / NULLIF((SELECT total_others FROM OtherStats), 0), 2)
        ELSE COUNT(*)
    END AS usage_count
FROM LabeledData
GROUP BY hour, activity_source
ORDER BY hour ASC;
```


After running the queries you should see some data if someone actually ran a command.

On the **Data Output** tab. There should be another tab called **Graph Visualizer**. Click it.

### Select:
- **Graph Type**: Stacked Line Chart
- **X**: hour
- **Y**: usage_count


Then click on generate and it should show up a graph.

## License

This project is provided as-is for educational purposes.

## Support

For issues or questions, please create an issue in the project repository.

---

Made with ❤️ for movie lovers and Discord communities! 🎬🍿


## TODO
- Mortal Kombat II 
- Toy story 5