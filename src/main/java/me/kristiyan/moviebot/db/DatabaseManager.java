package me.kristiyan.moviebot.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static HikariDataSource dataSource;

    public static void initialize() {
        // Initialize HikariCP connection pool
        HikariConfig config = new HikariConfig();

        String dbHost = System.getenv("POSTGRES_DB_HOST");
        String dbPort = System.getenv("POSTGRES_DB_PORT");
        String dbUser = System.getenv("POSTGRES_DB_USERNAME");
        String dbPassword = System.getenv("POSTGRES_DB_PASSWORD");
        String dbName = System.getenv("POSTGRES_DB_NAME");

        // Validate environment variables
        if (dbHost == null || dbPort == null || dbUser == null || dbPassword == null) {
            throw new RuntimeException("Missing required database environment variables. " +
                    "Required: POSTGRES_DB_HOST, POSTGRES_DB_PORT, POSTGRES_DB_USERNAME, POSTGRES_DB_PASSWORD, POSTGRES_DB_NAME");
        }

        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", dbHost, dbPort, dbName);
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(dbUser);
        config.setPassword(dbPassword);

        // HikariCP settings
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        dataSource = new HikariDataSource(config);

        createTables();

        System.out.println("Database initialized successfully!");
    }

    private static void createTables() {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();

            // Table for movies
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS movies (
                    id SERIAL PRIMARY KEY,
                    title TEXT NOT NULL UNIQUE,
                    gif_url TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Table to track current movie for each guild
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS active_movies (
                    guild_id TEXT PRIMARY KEY,
                    movie_id INTEGER NOT NULL REFERENCES movies(id),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Table to track who has already guessed for the current movie
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS active_guesses (
                    guild_id TEXT NOT NULL,
                    member_id TEXT NOT NULL,
                    guessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (guild_id, member_id)
                )
            """);

            // Table for guild leaderboard
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS guild_leaderboard (
                    guild_id TEXT PRIMARY KEY,
                    guild_name TEXT NOT NULL,
                    correct_guesses INTEGER DEFAULT 0
                )
            """);

            // Table for member leaderboard per guild
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS member_leaderboard (
                    guild_id TEXT NOT NULL,
                    member_id TEXT NOT NULL,
                    member_nickname TEXT NOT NULL,
                    correct_guesses INTEGER DEFAULT 0,
                    PRIMARY KEY (guild_id, member_id)
                )
            """);

            // Create indexes for better query performance
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_active_movies_guild ON active_movies(guild_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_active_guesses_guild ON active_guesses(guild_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_guild_leaderboard_score ON guild_leaderboard(correct_guesses DESC)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_member_leaderboard_guild ON member_leaderboard(guild_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_member_leaderboard_score ON member_leaderboard(guild_id, correct_guesses DESC)");

        } catch (SQLException e) {
            System.err.println("Database table creation error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create database tables", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database not initialized. Call initialize() first.");
        }
        return dataSource.getConnection();
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Database connection pool shut down.");
        }
    }

    // Set active movie for a guild (using movie from database)
    public static void setActiveMovie(String guildId, int movieId) {
        String sql = "INSERT INTO active_movies (guild_id, movie_id) VALUES (?, ?) " +
                "ON CONFLICT (guild_id) DO UPDATE SET movie_id = EXCLUDED.movie_id, created_at = CURRENT_TIMESTAMP";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, guildId);
            pstmt.setInt(2, movieId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error setting active movie: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Get active movie for a guild
    public static ActiveMovie getActiveMovie(String guildId) {
        String sql = """
            SELECT m.id, m.title, m.gif_url 
            FROM active_movies am
            JOIN movies m ON am.movie_id = m.id
            WHERE am.guild_id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, guildId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new ActiveMovie(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("gif_url")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error getting active movie: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Check if a member has already guessed for the current movie
    public static boolean hasAlreadyGuessed(String guildId, String memberId) {
        String sql = "SELECT 1 FROM active_guesses WHERE guild_id = ? AND member_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, guildId);
            pstmt.setString(2, memberId);
            ResultSet rs = pstmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.err.println("Error checking guess status: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Record that a member has made a guess
    public static void recordGuessAttempt(String guildId, String memberId) {
        String sql = "INSERT INTO active_guesses (guild_id, member_id) VALUES (?, ?) ON CONFLICT DO NOTHING";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, guildId);
            pstmt.setString(2, memberId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error recording guess attempt: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Remove active movie for a guild and clear all guesses
    public static void clearActiveMovie(String guildId) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Clear the movie
                String movieSql = "DELETE FROM active_movies WHERE guild_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(movieSql)) {
                    pstmt.setString(1, guildId);
                    pstmt.executeUpdate();
                }

                // Clear all guesses for this guild
                String guessesSql = "DELETE FROM active_guesses WHERE guild_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(guessesSql)) {
                    pstmt.setString(1, guildId);
                    pstmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("Error clearing active movie: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Record a correct guess
    public static void recordCorrectGuess(String guildId, String guildName, String memberId, String memberNickname) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Update guild leaderboard
                String guildSql = """
                    INSERT INTO guild_leaderboard (guild_id, guild_name, correct_guesses) 
                    VALUES (?, ?, 1)
                    ON CONFLICT(guild_id) DO UPDATE SET 
                        guild_name = EXCLUDED.guild_name,
                        correct_guesses = guild_leaderboard.correct_guesses + 1
                """;
                try (PreparedStatement pstmt = conn.prepareStatement(guildSql)) {
                    pstmt.setString(1, guildId);
                    pstmt.setString(2, guildName);
                    pstmt.executeUpdate();
                }

                // Update member leaderboard
                String memberSql = """
                    INSERT INTO member_leaderboard (guild_id, member_id, member_nickname, correct_guesses) 
                    VALUES (?, ?, ?, 1)
                    ON CONFLICT(guild_id, member_id) DO UPDATE SET 
                        member_nickname = EXCLUDED.member_nickname,
                        correct_guesses = member_leaderboard.correct_guesses + 1
                """;
                try (PreparedStatement pstmt = conn.prepareStatement(memberSql)) {
                    pstmt.setString(1, guildId);
                    pstmt.setString(2, memberId);
                    pstmt.setString(3, memberNickname);
                    pstmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("Error recording correct guess: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Get top guilds by correct guesses
    public static List<LeaderboardEntry> getGuildLeaderboard(int limit) {
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        String sql = "SELECT guild_name, correct_guesses FROM guild_leaderboard ORDER BY correct_guesses DESC LIMIT ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                leaderboard.add(new LeaderboardEntry(
                        rs.getString("guild_name"),
                        rs.getInt("correct_guesses")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error getting guild leaderboard: " + e.getMessage());
            e.printStackTrace();
        }

        return leaderboard;
    }

    // Get top members for a specific guild
    public static List<LeaderboardEntry> getMemberLeaderboard(String guildId, int limit) {
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        String sql = "SELECT member_nickname, correct_guesses FROM member_leaderboard WHERE guild_id = ? ORDER BY correct_guesses DESC LIMIT ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, guildId);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                leaderboard.add(new LeaderboardEntry(
                        rs.getString("member_nickname"),
                        rs.getInt("correct_guesses")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error getting member leaderboard: " + e.getMessage());
            e.printStackTrace();
        }

        return leaderboard;
    }

    // Get a random movie from the database
    public static Movie getRandomMovie() {
        String sql = "SELECT id, title, gif_url FROM movies ORDER BY RANDOM() LIMIT 1";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return new Movie(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("gif_url")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error getting random movie: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Add a new movie to the database
    public static boolean addMovie(String title, String gifUrl) {
        String sql = "INSERT INTO movies (title, gif_url) VALUES (?, ?) ON CONFLICT (title) DO NOTHING";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setString(2, gifUrl);
            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error adding movie: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Get total movie count
    public static int getMovieCount() {
        String sql = "SELECT COUNT(*) FROM movies";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error getting movie count: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    // Helper classes
    public static class ActiveMovie {
        public final int id;
        public final String title;
        public final String gifUrl;

        public ActiveMovie(int id, String title, String gifUrl) {
            this.id = id;
            this.title = title;
            this.gifUrl = gifUrl;
        }
    }

    public static class Movie {
        public final int id;
        public final String title;
        public final String gifUrl;

        public Movie(int id, String title, String gifUrl) {
            this.id = id;
            this.title = title;
            this.gifUrl = gifUrl;
        }
    }

    public static class LeaderboardEntry {
        public final String name;
        public final int score;

        public LeaderboardEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }
}