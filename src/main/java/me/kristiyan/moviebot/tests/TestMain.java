package me.kristiyan.moviebot.tests;

import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.services.ServiceManager;
import net.dv8tion.jda.api.JDA;

public class TestMain {
    private final DatabaseManager databaseManager;
    private final ServiceManager serviceManager;
    private final JDA jda;

    public TestMain(DatabaseManager databaseManager, ServiceManager serviceManager, JDA jda) {
        this.databaseManager = databaseManager;
        this.jda = jda;
        this.serviceManager = serviceManager;

        new GuessCommandTest(serviceManager).runGuessScenarios();
        new SkipCommandTest(serviceManager).runSkipScenarios();
        new StartCommandTest(serviceManager).runStartScenarios();
        new UpdatesCommandTest(serviceManager).runUpdateScenarios();
        new LeaderboardCommandTest(serviceManager, databaseManager).runLeaderboardScenarios();

    }

    public void testMovieReport() {
        new MovieReportCommandTest(serviceManager, jda).runReportScenarios();
    }
}
