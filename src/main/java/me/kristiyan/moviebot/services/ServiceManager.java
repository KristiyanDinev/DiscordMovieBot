package me.kristiyan.moviebot.services;

import me.kristiyan.moviebot.database.DatabaseManager;

public class ServiceManager {
    private final ActiveGameService activeGameService;
    private final LeaderboardsService leaderboardsService;
    private final MovieService movieService;
    private final UpdateAnnouncementService updateAnnouncementService;
    private final MovieReportService movieReportService;
    private final BotUsageService botUsageService;

    public ServiceManager(DatabaseManager databaseManager) {
        activeGameService = new ActiveGameService(databaseManager);
        leaderboardsService = new LeaderboardsService(databaseManager, this);
        movieService = new MovieService(databaseManager);
        updateAnnouncementService = new UpdateAnnouncementService(databaseManager);
        movieReportService = new MovieReportService(databaseManager);
        botUsageService = new BotUsageService(databaseManager);
    }

    public ActiveGameService getActiveGameService() {
        return activeGameService;
    }

    public LeaderboardsService getLeaderboardsService() {
        return leaderboardsService;
    }

    public MovieService getMovieService() {
        return movieService;
    }


    public UpdateAnnouncementService getUpdateAnnouncementService() {
        return updateAnnouncementService;
    }

    public MovieReportService getMovieReportService() {
        return movieReportService;
    }

    public BotUsageService getBotUsageService() {
        return botUsageService;
    }
}
