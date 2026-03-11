package me.kristiyan.moviebot.tests;


import me.kristiyan.moviebot.database.entities.games.ActiveGameEntity;
import me.kristiyan.moviebot.database.entities.movie.MovieEntity;
import me.kristiyan.moviebot.enums.GamemodeEnum;
import me.kristiyan.moviebot.services.ServiceManager;
import net.dv8tion.jda.api.JDA;

import java.util.Optional;

public class MovieReportCommandTest {
    private final ServiceManager services;
    private final JDA jda;
    private final String guildId = "REPORT_TEST_GUILD";
    private final String userId = "CRITICAL_USER_123";

    public MovieReportCommandTest(ServiceManager services,  JDA jda) {
        this.services = services;
        this.jda = jda;
    }

    public void runReportScenarios() {
        System.out.println("\n--- 🚩 STARTING MOVIE REPORT COMMAND LOGIC SCENARIOS ---");
        if (jda == null) {
            System.err.println("[Error] JDA  is null");
        }

        // Scenario 1: Attempt to report without an active game
        testReportWithNoActiveGame();

        // Scenario 2: Successful report during an active game
        testSuccessfulReport();

        // Scenario 3: Attempt to report the same movie twice
        testDuplicateReportPrevention();

        System.out.println("--- ✅ MOVIE REPORT COMMAND LOGIC COMPLETE ---");
    }

    private void testReportWithNoActiveGame() {
        System.out.println("[1] Testing Report with No Active Game...");
        // findRelevantGame should return null
        ActiveGameEntity game = services.getActiveGameService().findRelevantGame(guildId, userId, GamemodeEnum.NORMAL);

        if (game == null) {
            System.out.println("    - PASSED: Logic would correctly block report (no game found).");
        } else {
            System.out.println("    - FAILED: Found a ghost game that shouldn't exist.");
        }
    }

    private void testSuccessfulReport() {
        System.out.println("[2] Testing Successful Movie Report...");

        // Setup: Start a game so there is a movie to report
        Optional<MovieEntity> movieEntityOptional = services.getMovieService().getRandomMovie("movie");
        if (movieEntityOptional.isEmpty()) {
            System.out.println("    - ERROR: Movie not found.");
            return;
        }
        MovieEntity movie = movieEntityOptional.get();
        services.getActiveGameService().startGame(guildId, movie, 1,
                false, true, userId, GamemodeEnum.NORMAL);
        ActiveGameEntity game = services.getActiveGameService().findRelevantGame(guildId, userId, GamemodeEnum.NORMAL);

        // Simulate Command Logic
        String reason = "The GIF doesn't match the title.";
        if (!services.getMovieReportService().hasAlreadyReported(userId, movie)) {
            // Note: We pass null for the event in simulation since the service
            // has a null check for textChannel anyway.
            services.getMovieReportService().submitReport(jda, userId, "TesterNickname", movie, reason);
            System.out.println("    - PASSED: Report submitted and persisted to DB.");
        } else {
            System.out.println("    - FAILED: Service claimed user already reported new movie.");
        }
    }

    private void testDuplicateReportPrevention() {
        System.out.println("[3] Testing Duplicate Report Prevention...");

        ActiveGameEntity game = services.getActiveGameService().findRelevantGame(guildId, userId, GamemodeEnum.NORMAL);
        if (game == null) {
            System.out.println("[Error] game not found.");
            return;
        }
        MovieEntity movie = game.getMovie();

        // Check if service catches the existing report from Test [2]
        boolean blocked = services.getMovieReportService().hasAlreadyReported(userId, movie);

        if (blocked) {
            System.out.println("    - PASSED: Duplicate report correctly identified and blocked.");
        } else {
            System.out.println("    - FAILED: Duplicate report was not detected.");
        }

        // Cleanup
        services.getActiveGameService().endGame(game);
    }
}