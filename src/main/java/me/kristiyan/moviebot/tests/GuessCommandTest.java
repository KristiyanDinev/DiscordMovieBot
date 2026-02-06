package me.kristiyan.moviebot.tests;

import me.kristiyan.moviebot.database.entities.games.ActiveGameEntity;
import me.kristiyan.moviebot.database.entities.movie.MovieEntity;
import me.kristiyan.moviebot.enums.LeaderboardMemberStatus;
import me.kristiyan.moviebot.services.ServiceManager;

public class GuessCommandTest {
    private final ServiceManager services;
    private final String guildId = "GUESS_TEST_GUILD";
    private final String guildName = "Cinema Paradise";
    private final String userId = "USER_GUESSER_1";
    private final String userName = "Bob";

    public GuessCommandTest(ServiceManager services) {
        this.services = services;
    }

    public void runGuessScenarios() {
        System.out.println("\n--- 🧠 STARTING GUESS COMMAND LOGIC SCENARIOS ---");

        // Scenario 1: Multi-round Game Progression (Correct Guess)
        testMultiRoundProgression();

        // Scenario 2: Game Completion (Last Round Correct)
        testGameCompletion();

        // Scenario 3: High Stakes Instant Failure (Wrong Guess)
        testHighStakesFailure();

        System.out.println("--- ✅ GUESS COMMAND LOGIC COMPLETE ---");
    }

    private void testMultiRoundProgression() {
        System.out.println("[1] Testing Round Progression (Correct Guess)...");

        // Setup: 2-round game
        MovieEntity movie = services.getMovieService().getRandomMovie("movie");
        services.getActiveGameService().startGame(guildId, movie, 2, false, false, userId);

        ActiveGameEntity game = services.getActiveGameService().findRelevantGame(guildId, userId);
        if (game == null) {
            System.out.println("[ERROR] Game not found");
            return;
        }
        String correctTitle = game.getMovie().getTitle();

        // Simulate logic inside handleGuessCommand for a correct guess (not game over)
        System.out.println("    - Current Round: " + game.getCurrentRound());

        // recordGuessAttempt happens
        services.getActiveGameService().recordGuessAttempt(guildId, userId, userName,
                game.getCurrentRound(), game);

        // Progress state
        int nextRound = game.getCurrentRound() + 1;
        game.setCurrentRound(nextRound);
        MovieEntity nextMovie = services.getMovieService().getRandomMovie("movie");
        game.setMovie(nextMovie);
        services.getActiveGameService().updateActiveGame(game);

        // Verify
        ActiveGameEntity updatedGame = services.getActiveGameService().findRelevantGame(guildId, userId);
        if (updatedGame == null) {
            System.out.println("[ERROR] Updates game not found");
            return;
        }
        System.out.println("    - Next Round set to: " + updatedGame.getCurrentRound());
        System.out.println("    - New movie assigned: " + !updatedGame.getMovie().getTitle().equals(correctTitle));

        services.getActiveGameService().endGame(updatedGame);
    }

    private void testGameCompletion() {
        System.out.println("[2] Testing Game Completion (Last Round Correct)...");

        // Setup: 1-round game
        MovieEntity movie = services.getMovieService().getRandomMovie("movie");
        services.getActiveGameService().startGame(guildId, movie, 1, false, false, null);
        ActiveGameEntity game = services.getActiveGameService().findRelevantGame(guildId, userId);

        // Simulate logic: record guesses, update leaderboard, end game
        services.getLeaderboardsService().recordCorrectGuesses(guildId, guildName, LeaderboardMemberStatus.TOP_GUESSES);
        services.getActiveGameService().endGame(game);

        // Verify
        ActiveGameEntity nullGame = services.getActiveGameService().findRelevantGame(guildId, userId);
        System.out.println("    - Game ended and cleaned up: " + (nullGame == null));
    }

    private void testHighStakesFailure() {
        System.out.println("[3] Testing High Stakes Failure (Wrong Guess)...");

        // Setup: High Stakes Game
        MovieEntity movie = services.getMovieService().getRandomMovie("movie");
        services.getActiveGameService().startGame(guildId, movie, 5, true, false, null);
        ActiveGameEntity game = services.getActiveGameService().findRelevantGame(guildId, userId);

        // Simulate Logic: Wrong guess in High Stakes triggers record and endGame
        services.getLeaderboardsService().recordCorrectGuesses(guildId, guildName, LeaderboardMemberStatus.TOP_GUESSES_HIGH_STAKES);
        services.getActiveGameService().endGame(game);

        // Verify
        ActiveGameEntity nullGame = services.getActiveGameService().findRelevantGame(guildId, userId);
        System.out.println("    - High Stakes game terminated after failure: " + (nullGame == null));
    }
}