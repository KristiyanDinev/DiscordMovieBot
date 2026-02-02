package me.kristiyan.moviebot.services;

import me.kristiyan.moviebot.database.DatabaseManager;

public class ServiceManager {
    private final ActiveGameService activeGameService;
    private final LeaderboardsService leaderboardsService;
    private final MovieService movieService;
    private final PremiumService premiumService;
    private final UpdateAnnouncementService updateAnnouncementService;

    public ServiceManager(DatabaseManager databaseManager) {
        activeGameService = new ActiveGameService(databaseManager);
        leaderboardsService = new LeaderboardsService(databaseManager);
        movieService = new MovieService(databaseManager);
        premiumService = new PremiumService(databaseManager);
        updateAnnouncementService = new UpdateAnnouncementService(databaseManager);
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

    public PremiumService getPremiumService() {
        return premiumService;
    }

    public UpdateAnnouncementService getUpdateAnnouncementService() {
        return updateAnnouncementService;
    }
}
