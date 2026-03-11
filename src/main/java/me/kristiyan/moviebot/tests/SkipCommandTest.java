package me.kristiyan.moviebot.tests;

import me.kristiyan.moviebot.database.entities.games.ActiveGameEntity;
import me.kristiyan.moviebot.database.entities.movie.MovieEntity;
import me.kristiyan.moviebot.services.ServiceManager;

import java.util.Optional;

public class SkipCommandTest {
    private final ServiceManager services;
    private final String guildId = "SKIP_TEST_GUILD";
    private final String userId = "USER_SKIPPER_1";

    public SkipCommandTest(ServiceManager services) {
        this.services = services;
    }

    public void runSkipScenarios() {
        System.out.println("\n--- ⏭️ STARTING SKIP COMMAND LOGIC SCENARIOS ---");

        // Scenario 1: Successful Skip in Standard Mode
        testStandardSkip();

        // Scenario 2: Blocked Skip in High Stakes Mode
        testHighStakesSkipBlocked();

        // Scenario 3: Skip Persistence Check
        testSkipPersistence();

        System.out.println("--- ✅ SKIP COMMAND LOGIC COMPLETE ---");
    }

    private void testStandardSkip() {
        System.out.println("[1] Testing Standard Skip...");

        // Setup: Start a standard 5-round game
        Optional<MovieEntity> movieAOptional = services.getMovieService().getRandomMovie("movie");
        if (movieAOptional.isEmpty()) {
            System.out.println("    - ERROR: Movie not found.");
            return;
        }
        MovieEntity movieA = movieAOptional.get();
        services.getActiveGameService().startGame(guildId, movieA, 5, false, false, null);

        ActiveGameEntity game = services.getActiveGameService().findRelevantGame(guildId, userId);
        if (game == null) {
            System.out.println("[ERROR] Game not found");
            return;
        }
        int firstMovieId = game.getMovie().getId();

        // Simulate Skip logic
        Optional<MovieEntity> movieBOptional = services.getMovieService().getRandomMovie("movie");
        if (movieBOptional.isEmpty()) {
            System.out.println("    - ERROR: Movie not found.");
            return;
        }
        MovieEntity movieB = movieBOptional.get();
        game.setMovie(movieB);
        services.getActiveGameService().updateActiveGame(game);

        // Verify
        ActiveGameEntity updatedGame = services.getActiveGameService().findRelevantGame(guildId, userId);
        if (updatedGame == null) {
            System.out.println("[ERROR] Updated Game not found");
            return;
        }
        System.out.println("    - Previous Movie ID: " + firstMovieId);
        System.out.println("    - New Movie ID: " + updatedGame.getMovie().getId());
        System.out.println("    - Round remains at: " + updatedGame.getCurrentRound());

        services.getActiveGameService().endGame(updatedGame);
    }

    private void testHighStakesSkipBlocked() {
        System.out.println("[2] Testing High Stakes Skip Restriction...");

        // Setup: Start a High Stakes game
        Optional<MovieEntity> movieEntityOptional = services.getMovieService().getRandomMovie("movie");
        if (movieEntityOptional.isEmpty()) {
            System.out.println("    - ERROR: Movie not found.");
            return;
        }
        MovieEntity movie = movieEntityOptional.get();
        services.getActiveGameService().startGame(guildId, movie, 3, true, false, null);

        ActiveGameEntity game = services.getActiveGameService().findRelevantGame(guildId, userId);
        if (game == null) {
            System.out.println("[ERROR] Game not found");
            return;
        }

        // Logic verification: The command handler would check isHighStakes()
        if (game.isHighStakes()) {
            System.out.println("    - PASSED: High Stakes flag detected. Command would block skip.");
        } else {
            System.out.println("    - FAILED: High Stakes flag not detected.");
        }

        services.getActiveGameService().endGame(game);
    }

    private void testSkipPersistence() {
        System.out.println("[3] Testing Skip DB Persistence...");

        Optional<MovieEntity> movieOriginalOptional = services.getMovieService().getRandomMovie("movie");
        if (movieOriginalOptional.isEmpty()) {
            System.out.println("    - ERROR: Movie not found.");
            return;
        }
        MovieEntity movieOriginal = movieOriginalOptional.get();
        services.getActiveGameService().startGame(guildId, movieOriginal, 1, false, true, userId);

        ActiveGameEntity game = services.getActiveGameService().findRelevantGame(guildId, userId);
        if (game == null) {
            System.out.println("[ERROR] Game not found");
            return;
        }
        Optional<MovieEntity> movieNewOptional = services.getMovieService().getRandomMovie("movie");
        if (movieNewOptional.isEmpty()) {
            System.out.println("    - ERROR: Movie not found.");
            return;
        }
        MovieEntity movieNew = movieNewOptional.get();
        game.setMovie(movieNew);
        services.getActiveGameService().updateActiveGame(game);

        // Simulate a new session/lookup to ensure it saved to DB
        ActiveGameEntity reloadedGame = services.getActiveGameService().findRelevantGame(guildId, userId);
        if (reloadedGame == null) {
            System.out.println("[ERROR] Reloaded Game not found");
            return;
        }
        boolean isPersistent = reloadedGame.getMovie().getId() == movieNew.getId();

        System.out.println("    - Skip Persistence: " + (isPersistent ? "SUCCESS" : "FAILURE"));

        services.getActiveGameService().endGame(reloadedGame);
    }
}
