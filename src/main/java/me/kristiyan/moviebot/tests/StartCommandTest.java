package me.kristiyan.moviebot.tests;

import me.kristiyan.moviebot.database.entities.movie.MovieEntity;
import me.kristiyan.moviebot.services.ServiceManager;

public class StartCommandTest {
    private final ServiceManager services;
    private final String guildId = "START_TEST_GUILD";

    public StartCommandTest(ServiceManager services) {
        this.services = services;
    }

    public void runStartScenarios() {
        System.out.println("\n--- 🎬 STARTING START COMMAND LOGIC SCENARIOS ---");

        // Scenario 1: Successful Public Game Start
        testPublicGameStart();

        // Scenario 2: Blocked Public Game (Concurrency check)
        testPublicGameConcurrency();

        // Scenario 3: Successful Locked Game Start
        testLockedGameStart();

        // Scenario 4: Multiple Locked Games (Different Users)
        testMultipleLockedGames();

        System.out.println("--- ✅ START COMMAND LOGIC COMPLETE ---");
    }

    private void testPublicGameStart() {
        System.out.println("[1] Testing Public Game Start...");

        // Initial state: No public game
        boolean hasPublic = services.getActiveGameService().hasActivePublicGame(guildId);
        System.out.println("    - Public game exists initially: " + hasPublic);

        MovieEntity movie = services.getMovieService().getRandomMovie("movie");
        boolean started = services.getActiveGameService().startGame(guildId, movie, 3, false, false, null);

        System.out.println("    - Game Start result: " + started);
        System.out.println("    - Public game exists now: " + services.getActiveGameService().hasActivePublicGame(guildId));
    }

    private void testPublicGameConcurrency() {
        System.out.println("[2] Testing Public Game Concurrency Block...");

        // Try to start a second public game in the same guild
        boolean hasPublic = services.getActiveGameService().hasActivePublicGame(guildId);
        if (hasPublic) {
            System.out.println("    - PASSED: Command logic would see existing public game and return early.");
        } else {
            System.out.println("    - FAILED: Should have found an existing public game.");
        }
    }

    private void testLockedGameStart() {
        System.out.println("[3] Testing Locked Game Start...");

        // User starts a private game while a public one exists (Should be allowed)
        String userId = "USER_STARTER_1";
        boolean hasLocked = services.getActiveGameService().hasActiveLockedGame(guildId, userId);
        System.out.println("    - User already has locked game: " + hasLocked);

        MovieEntity movie = services.getMovieService().getRandomMovie("movie");
        boolean started = services.getActiveGameService().startGame(guildId, movie, 1, true, true, userId);

        System.out.println("    - Locked Game Started: " + started);
        System.out.println("    - User has locked game now: " + services.getActiveGameService().hasActiveLockedGame(guildId, userId));
    }

    private void testMultipleLockedGames() {
        System.out.println("[4] Testing Multiple Locked Games (User Isolation)...");
        String user2 = "USER_STARTER_2";

        // User 2 starts their own locked game in the same guild
        MovieEntity movie = services.getMovieService().getRandomMovie("movie");
        boolean user2Started = services.getActiveGameService().startGame(guildId, movie, 1, false, true, user2);

        System.out.println("    - User 2 Locked Game Started: " + user2Started);

        // Cleanup all games in this guild for this test suite
        cleanupAll(user2);
    }

    private void cleanupAll(String u2) {
        // Find and end public game
        var publicGame = services.getActiveGameService().findRelevantGame("START_TEST_GUILD", "TEMP");
        if (publicGame != null && !publicGame.isLocked()) services.getActiveGameService().endGame(publicGame);

        // Find and end locked games
        var g1 = services.getActiveGameService().findRelevantGame("START_TEST_GUILD", "USER_STARTER_1");
        if (g1 != null) services.getActiveGameService().endGame(g1);

        var g2 = services.getActiveGameService().findRelevantGame("START_TEST_GUILD", u2);
        if (g2 != null) services.getActiveGameService().endGame(g2);
    }
}
