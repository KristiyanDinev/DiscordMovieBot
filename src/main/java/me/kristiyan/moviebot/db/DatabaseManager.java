package me.kristiyan.moviebot.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:movieguess.db";

    public static void initialize() {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();

            // Table to track current movie for each guild
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS active_movies (
                    guild_id TEXT PRIMARY KEY,
                    movie_title TEXT NOT NULL,
                    gif_url TEXT NOT NULL,
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

            System.out.println("Database initialized successfully!");

        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // Set active movie for a guild
    public static void setActiveMovie(String guildId, String movieTitle, String gifUrl) {

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO active_movies (guild_id, movie_title, gif_url) VALUES (?, ?, ?)")) {

            pstmt.setString(1, guildId);
            pstmt.setString(2, movieTitle);
            pstmt.setString(3, gifUrl);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error setting active movie: " + e.getMessage());
        }
    }

    // Get active movie for a guild
    public static ActiveMovie getActiveMovie(String guildId) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT movie_title, gif_url FROM active_movies WHERE guild_id = ?")) {

            pstmt.setString(1, guildId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new ActiveMovie(rs.getString("movie_title"), rs.getString("gif_url"));
            }

        } catch (SQLException e) {
            System.err.println("Error getting active movie: " + e.getMessage());
        }

        return null;
    }

    // Check if a member has already guessed for the current movie
    public static boolean hasAlreadyGuessed(String guildId, String memberId) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM active_guesses WHERE guild_id = ? AND member_id = ?")) {

            pstmt.setString(1, guildId);
            pstmt.setString(2, memberId);
            ResultSet rs = pstmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.err.println("Error checking guess status: " + e.getMessage());
        }

        return false;
    }

    // Record that a member has made a guess
    public static void recordGuessAttempt(String guildId, String memberId) {
        String sql = "INSERT OR IGNORE INTO active_guesses (guild_id, member_id) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, guildId);
            pstmt.setString(2, memberId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error recording guess attempt: " + e.getMessage());
        }
    }

    // Remove active movie for a guild and clear all guesses
    public static void clearActiveMovie(String guildId) {
        try (Connection conn = getConnection()) {
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

        } catch (SQLException e) {
            System.err.println("Error clearing active movie: " + e.getMessage());
        }
    }

    // Record a correct guess
    public static void recordCorrectGuess(String guildId, String guildName, String memberId, String memberNickname) {
        try (Connection conn = getConnection()) {
            // Update guild leaderboard
            try (PreparedStatement pstmt = conn.prepareStatement("""
                INSERT INTO guild_leaderboard (guild_id, guild_name, correct_guesses) 
                VALUES (?, ?, 1)
                ON CONFLICT(guild_id) DO UPDATE SET 
                    guild_name = excluded.guild_name,
                    correct_guesses = correct_guesses + 1
            """)) {
                pstmt.setString(1, guildId);
                pstmt.setString(2, guildName);
                pstmt.executeUpdate();
            }

            // Update member leaderboard
            try (PreparedStatement pstmt = conn.prepareStatement("""
                INSERT INTO member_leaderboard (guild_id, member_id, member_nickname, correct_guesses) 
                VALUES (?, ?, ?, 1)
                ON CONFLICT(guild_id, member_id) DO UPDATE SET 
                    member_nickname = excluded.member_nickname,
                    correct_guesses = correct_guesses + 1
            """)) {
                pstmt.setString(1, guildId);
                pstmt.setString(2, memberId);
                pstmt.setString(3, memberNickname);
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println("Error recording correct guess: " + e.getMessage());
        }
    }

    // Get top guilds by correct guesses
    public static List<LeaderboardEntry> getGuildLeaderboard(int limit) {
        List<LeaderboardEntry> leaderboard = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT guild_name, correct_guesses FROM guild_leaderboard ORDER BY correct_guesses DESC LIMIT ?")) {

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
        }

        return leaderboard;
    }

    // Get top members for a specific guild
    public static List<LeaderboardEntry> getMemberLeaderboard(String guildId, int limit) {
        List<LeaderboardEntry> leaderboard = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT member_nickname, correct_guesses FROM member_leaderboard WHERE guild_id = ? ORDER BY correct_guesses DESC LIMIT ?")) {

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
        }

        return leaderboard;
    }

    // Helper classes
    public static class ActiveMovie {
        public final String title;
        public final String gifUrl;

        public ActiveMovie(String title, String gifUrl) {
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