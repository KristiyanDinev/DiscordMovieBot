# Architecture Overview

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Discord API                           │
└─────────────────┬───────────────────────────────────────────┘
                  │
                  │ Gateway Connection
                  │ 
                  │
┌─────────────────▼───────────────────────────────────────────┐
│                   MovieGuessBot (Main)                       │
│  - Initializes JDA                                           │
│  - Registers slash commands                                  │
│  - Sets up database                                          │
└─────────────────┬───────────────────────────────────────────┘
                  │
                  │ Event Listener
                  │
┌─────────────────▼───────────────────────────────────────────┐
│                  CommandListener                             │
│  - Handles /start command                                    │
│  - Handles /guess command (ONE ATTEMPT ONLY)                 │
│  - Handles /leaderboard command                              │
│  - Handles /skip command                                     │
└─────┬───────────┬──────────────┬─────────────┬──────────────┘
      │           │              │             │
      │           │              │             │
┌─────▼─────┐ ┌──▼──────────┐ ┌─▼────────┐ ┌─▼──────────────┐
│  Movie    │ │  Database   │ │  Movie   │ │  Discord       │
│  Provider │ │  Manager    │ │  Matcher │ │  Embed         │
└───────────┘ └──────┬──────┘ └──────────┘ └────────────────┘
                     │
                     │
              ┌──────▼─────────┐
              │  SQLite DB     │
              │  - active_     │
              │    movies      │
              │  - active_     │
              │    guesses (NEW)│
              │  - guild_      │
              │    leaderboard │
              │  - member_     │
              │    leaderboard │
              └────────────────┘
```

## Key Architectural Changes

### One-Guess-Only Implementation

The bot now enforces a strict one-guess-per-member-per-movie rule:

1. **active_guesses Table**: New table tracks who has guessed for the current movie
2. **Guess Validation**: Before processing a guess, bot checks if member already guessed
3. **Immediate Resolution**:
    - ✅ Correct guess → Score increases, round ends
    - ❌ Wrong guess → Answer revealed, round ends, no score
4. **Cleanup**: When a round ends (correct, wrong, or skip), all guess records are cleared

## Component Responsibilities

### 1. MovieGuessBot.java (Main Entry Point)
- **Purpose**: Bootstrap and configure the bot
- **Responsibilities**:
    - Initialize database connection
    - Create JDA instance with minimal intents
    - Register event listeners
    - Register slash commands
    - Handle graceful shutdown

### 2. CommandListener.java (Event Handler)
- **Purpose**: Handle all slash command interactions
- **Responsibilities**:
    - Process `/start` command → Get random movie, store in DB, show GIF
    - Process `/guess` command → Check if already guessed, validate answer
    - Process `/leaderboard` command → Fetch and display stats
    - Process `/skip` command → Reveal answer, clear guesses, start new round
    - Build and send Discord embeds

### 3. DatabaseManager.java (Data Layer)
- **Purpose**: Manage all database operations
- **Responsibilities**:
    - Initialize database schema
    - CRUD operations for active movies
    - Track who has guessed (NEW!)
    - Track guild leaderboard (server stats)
    - Track member leaderboard (player stats per server)
    - Handle SQLite connections and statements

### 4. MovieProvider.java (Data Provider)
- **Purpose**: Provide movie data
- **Responsibilities**:
    - Store collection of popular movies with GIF URLs
    - Return random movie for new game rounds
    - Can be easily extended with more movies

### 5. MovieMatcher.java (Matching Algorithm)
- **Purpose**: Intelligent movie title matching
- **Responsibilities**:
    - Normalize user input (remove special chars, lowercase, etc.)
    - Fuzzy matching using Levenshtein distance
    - Handle partial matches
    - Ignore common articles (the, a, an)
    - Accept typos within threshold

## Data Flow

### Starting a Game (`/start`)
```
User → Discord → CommandListener.handleStartCommand()
                      ↓
                 Check for active movie
                      ↓
                 MovieProvider.getRandomMovie()
                      ↓
                 DatabaseManager.setActiveMovie()
                      ↓
                 SQLite: INSERT active_movies
                      ↓
                 Send GIF Embed → All Users
```

### Making a Guess (`/guess`) - NEW FLOW!
```
User → Discord → CommandListener.handleGuessCommand()
                      ↓
                 DatabaseManager.getActiveMovie()
                      ↓
                 DatabaseManager.hasAlreadyGuessed()
                      ↓
            ┌─────────┴──────────┐
            ▼                    ▼
    Already Guessed       First Attempt
            │                    │
            ▼                    ▼
    Send "Already         DatabaseManager.
    Guessed" Msg          recordGuessAttempt()
    (ephemeral)                  │
                                 ▼
                          MovieMatcher.isMatch()
                                 │
                    ┌────────────┴───────────┐
                    ▼                        ▼
            Correct Match              Wrong Match
                    │                        │
                    ▼                        ▼
    DatabaseManager.              DatabaseManager.
    recordCorrectGuess()          clearActiveMovie()
                    │                        │
                    ▼                        ▼
    Clear Active Movie           Send "Wrong! Answer
    & All Guesses               was: TITLE" → All
                    │                        │
                    ▼                        ▼
    Send Success → All            Round Ends
    Users                         (No Score)
```

### Skipping a Movie (`/skip`)
```
User → Discord → CommandListener.handleSkipCommand()
                      ↓
                 DatabaseManager.getActiveMovie()
                      ↓
                 DatabaseManager.clearActiveMovie()
                      ↓
                 Clear all guesses for this guild
                      ↓
                 MovieProvider.getRandomMovie()
                      ↓
                 DatabaseManager.setActiveMovie()
                      ↓
                 Send "Skipped! Answer was: TITLE
                 Here's new movie" → All Users
```

## Gateway Intents Explanation

### Why We Only Need Two Intents

**GUILDS Intent** ✅
- Access to guild/server information
- Required to know which server the command came from
- Provides: Guild ID, Guild Name

**GUILD_MEMBERS Intent** ✅
- Access to member information
- Required to know who sent the command
- Provides: Member ID, Member Nickname (display name)

### What We DON'T Need ❌

- MESSAGE_CONTENT - We don't read message content
- GUILD_MESSAGES - We only use slash commands
- DIRECT_MESSAGES - No DM functionality
- GUILD_VOICE_STATES - No voice features
- And many others...

## Database Schema

### Table: active_movies
Stores the current movie being guessed in each server.

| Column      | Type      | Description                    |
|-------------|-----------|--------------------------------|
| guild_id    | TEXT (PK) | Discord server ID              |
| movie_title | TEXT      | Movie title to be guessed      |
| gif_url     | TEXT      | URL of the movie GIF           |
| created_at  | TIMESTAMP | When the game round started    |

### Table: active_guesses
Tracks who has guessed for the current movie in each server.

| Column      | Type              | Description                    |
|-------------|-------------------|--------------------------------|
| guild_id    | TEXT (PK)         | Discord server ID              |
| member_id   | TEXT (PK)         | Discord member/user ID         |
| guessed_at  | TIMESTAMP         | When the member made their guess|

**Note**: Composite primary key (guild_id, member_id) ensures each member can only have one guess record per server.

### Table: guild_leaderboard
Tracks total correct guesses per server.

| Column          | Type      | Description                    |
|-----------------|-----------|--------------------------------|
| guild_id        | TEXT (PK) | Discord server ID              |
| guild_name      | TEXT      | Server name (display)          |
| correct_guesses | INTEGER   | Total correct guesses          |

### Table: member_leaderboard
Tracks individual player performance per server.

| Column          | Type              | Description                    |
|-----------------|-------------------|--------------------------------|
| guild_id        | TEXT (PK)         | Discord server ID              |
| member_id       | TEXT (PK)         | Discord member/user ID         |
| member_nickname | TEXT              | Member's display name          |
| correct_guesses | INTEGER           | Player's correct guesses       |

**Note**: Composite primary key (guild_id, member_id) ensures each member is tracked per server.


A movie table with every movie with gif url there. It has indexing for faster results.

## Matching Algorithm Details

The `MovieMatcher` uses multiple strategies:

1. **Normalization**
    - Convert to lowercase
    - Remove diacritics/accents
    - Strip special characters
    - Trim whitespace

2. **Word Extraction**
    - Split into words
    - Remove articles (the, a, an)
    - Ignore words ≤2 characters

3. **Matching Strategies**
    - Exact match after normalization
    - All significant words present
    - Levenshtein distance (80% similarity)

### Example Matching

Movie: "The Lord of the Rings"

| Guess                  | Match? | Reason                          |
|------------------------|--------|---------------------------------|
| lord of the rings      | ✅     | All significant words present   |
| lord rings             | ✅     | All significant words present   |
| lotr                   | ❌     | Missing significant words       |
| the lord of ring       | ✅     | 80%+ Levenshtein similarity     |
| harry potter           | ❌     | Different words                 |

## Gradle Build System

### Why Gradle?

- **No Installation Required**: Gradle Wrapper downloads everything
- **Fast**: Incremental builds, dependency caching
- **Modern**: Industry standard
- **Cross-platform**: Works on Windows, Mac, Linux

### Build Structure

```
movie-guess-bot/
├── build.gradle              # Build configuration
├── settings.gradle           # Project settings
├── gradlew                   # Unix wrapper script
├── gradlew.bat              # Windows wrapper script
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── src/
│   └── main/
│       └── java/           # Java source files
└── build/
    └── libs/               # Generated JAR files
```

### Key Gradle Tasks

```bash
./gradlew build      # Compile, test, and build JAR
./gradlew clean      # Remove build artifacts
./gradlew shadowJar  # Create fat JAR with dependencies
./gradlew run        # Run bot directly
```

## Security Considerations

1. **Token Security**: Bot token passed as command-line argument (not hardcoded)
2. **Minimal Permissions**: Only requests necessary intents
3. **Data Privacy**: Only stores IDs, names, and game statistics
4. **SQL Injection**: Uses prepared statements
5. **Input Validation**: All user input validated before processing
6. **One Guess Enforcement**: Database-backed guess tracking prevents cheating

## Scalability Notes

- **Database**: SQLite suitable for small-medium bots (hundreds of servers)
- **For Large Scale**: Consider migrating to PostgreSQL/MySQL
- **Caching**: Could add in-memory cache for active movies
- **Rate Limiting**: JDA handles Discord rate limits automatically
- **Guess Tracking**: Efficient composite key prevents duplicate guesses

## Game Design Philosophy

The one-guess-only rule creates:
- **Higher Stakes**: Players must think carefully before guessing
- **Social Dynamics**: Team discussion before someone guesses
- **Learning**: Wrong guesses reveal answers, teaching everyone
- **Fairness**: Only correct first attempts count towards score
- **Engagement**: More exciting than unlimited attempts

---

This architecture ensures the bot is:
- ✅ Discord verified-compliant (minimal intents)
- ✅ Easy to maintain (clear separation of concerns)
- ✅ Scalable (modular design)
- ✅ Secure (no hardcoded secrets, prepared statements)
- ✅ Fair (one-guess enforcement, answer revelation)