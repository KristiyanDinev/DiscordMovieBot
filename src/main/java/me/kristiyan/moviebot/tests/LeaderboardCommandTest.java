package me.kristiyan.moviebot.tests;

import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.database.entities.games.ActiveGameEntity;
import me.kristiyan.moviebot.database.entities.games.ActiveGuessEntity;
import me.kristiyan.moviebot.database.entities.leaderboards.MemberStatusEntity;
import me.kristiyan.moviebot.database.entities.movie.MovieEntity;
import me.kristiyan.moviebot.enums.LeaderboardMemberStatus;
import me.kristiyan.moviebot.services.ServiceManager;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class LeaderboardCommandTest {
    private final ServiceManager services;
    private final DatabaseManager databaseManager;

    public LeaderboardCommandTest(ServiceManager services, DatabaseManager databaseManager) {
        this.services = services;
        this.databaseManager = databaseManager;
    }

    public void runLeaderboardScenarios() {
        System.out.println("\n--- 🏆 STARTING LEADERBOARD COMMAND LOGIC SCENARIOS ---");

        // Scenario 1: Aggregating Local Rankings
        testLocalMemberRanking();

        // Scenario 2: Aggregating Global Guild Rankings
        testGlobalGuildRanking();

        // Scenario 3: Empty State Handling
        testEmptyLeaderboard();

        testGuildAggregation();

        System.out.println("--- ✅ LEADERBOARD COMMAND LOGIC COMPLETE ---");
    }

    private void testLocalMemberRanking() {
        System.out.println("[1] Testing Local Member Rankings...");
        String guildId = "LOCKED_SERVER_1";

        // Setup: One user with more points than the other
        createScore(guildId, "Server One", "User_Alpha", 50);
        createScore(guildId, "Server One", "User_Beta", 100);

        List<MemberStatusEntity> board = services.getLeaderboardsService()
                .getMemberLeaderboard(guildId, 10, LeaderboardMemberStatus.TOP_GUESSES);

        // Verification
        if (!board.isEmpty() && board.getFirst().getMemberNickname().equals("User_Beta")) {
            System.out.println("    - PASSED: User_Beta is #1 with " + board.getFirst().getCorrectGuesses() + " points.");
        } else {
            System.out.println("    - FAILED: Ranking order is incorrect.");
        }
    }

    private void testGlobalGuildRanking() {
        System.out.println("[2] Testing Global Guild Rankings...");

        // Setup: Different guilds with different total scores
        createScore("G1", "Top Guild", "Leader1", 500);
        createScore("G2", "Noob Guild", "Leader2", 10);

        List<MemberStatusEntity> board = services.getLeaderboardsService()
                .getGuildLeaderboard(LeaderboardMemberStatus.TOP_GUESSES, 10);

        // Verification
        boolean firstIsTop = board.stream()
                .findFirst()
                .map(e -> e.getGuildName().equals("Top Guild"))
                .orElse(false);

        System.out.println("    - Global Ranking Check: " + (firstIsTop ? "PASSED" : "FAILED"));
    }

    private void testEmptyLeaderboard() {
        System.out.println("[3] Testing Empty State...");
        List<MemberStatusEntity> emptyBoard = services.getLeaderboardsService()
                .getMemberLeaderboard("G_EMPTY", 10, LeaderboardMemberStatus.TOP_GUESSES_HIGH_STAKES);

        System.out.println("    - Empty Check: " + (emptyBoard.isEmpty() ? "PASSED" : "FAILED"));
    }

    private void testGuildAggregation() {
        System.out.println("[4] Testing Guild Score Aggregation (No Duplicates)...");
        String guildId = "AGG_GUILD_1";
        String guildName = "United Movie Fans";

        // Setup: Two different users in the SAME guild with different scores
        // User A gets 10 points, User B gets 20 points. Total should be 30.
        createScore(guildId, guildName, "User_A", 10);
        createScore(guildId, guildName, "User_B", 20);

        List<MemberStatusEntity> board = services.getLeaderboardsService()
                .getGuildLeaderboard(LeaderboardMemberStatus.TOP_GUESSES, 10);

        // Filter for our specific guild in the global list
        List<MemberStatusEntity> guildEntries = board.stream()
                .filter(e -> e.getGuildId().equals(guildId))
                .toList();

        // Verification 1: The guild should only appear ONCE
        boolean isUnique = guildEntries.size() == 1;

        // Verification 2: The score should be the SUM (10 + 20 = 30)
        int totalScore = isUnique ? guildEntries.getFirst().getCorrectGuesses() : 0;

        if (isUnique && totalScore == 30) {
            System.out.println("    - PASSED: Guild aggregated to 1 entry with total score 30.");
        } else {
            System.out.println("    - FAILED: " + (isUnique ? "Incorrect sum: " + totalScore : "Duplicate guild entries found!"));
        }
    }

    /**
     * Injects dummy data into the leaderboard by simulating a completed game.
     */
    private void createScore(String gid, String gName, String mName, int score) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            MovieEntity dummyMovie = services.getMovieService().getRandomMovie("movie");

            ActiveGameEntity game = new ActiveGameEntity();
            game.setGuildId(gid);
            game.setMovie(dummyMovie);
            game.setTotalRounds(score);
            game.setHighStake(false);
            session.persist(game);

            for (int i = 1; i <= score; i++) {
                ActiveGuessEntity guess = new ActiveGuessEntity();
                guess.setActiveGame(game);
                guess.setGuildId(gid);
                guess.setMemberId(mName + "_ID");
                guess.setRoundNumber(i);
                // FIX 1: Match the nickname exactly to the test expectation
                guess.setMemberNickname(mName);
                session.persist(guess);
            }

            tx.commit();
            // Session closes here, forcing a flush to the DB
        }

        // FIX 2: Trigger aggregation logic AFTER the setup session is fully closed
        services.getLeaderboardsService().recordCorrectGuesses(gid, gName, LeaderboardMemberStatus.TOP_GUESSES);

        // FIX 3: Optional but helpful - verify the game existed
        ActiveGameEntity game = services.getActiveGameService().getActiveGameEntity(gid);
        if (game != null) {
            services.getActiveGameService().endGame(game);
        }
    }
}
