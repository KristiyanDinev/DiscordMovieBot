package me.kristiyan.moviebot.tests;

import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.services.ServiceManager;

public class TestMain {
    private final DatabaseManager databaseManager;
    private final ServiceManager serviceManager;

    public TestMain(DatabaseManager databaseManager, ServiceManager serviceManager) {
        this.databaseManager = databaseManager;
        this.serviceManager = serviceManager;
    }


}
