package me.kristiyan.moviebot.services;

import me.kristiyan.moviebot.database.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfileService {
    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);
    private final DatabaseManager databaseManager;

    public ProfileService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
}
