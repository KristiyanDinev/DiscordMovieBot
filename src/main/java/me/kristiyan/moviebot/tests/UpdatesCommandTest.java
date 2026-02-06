package me.kristiyan.moviebot.tests;

import me.kristiyan.moviebot.database.entities.updates.UpdateAnnouncementEntity;
import me.kristiyan.moviebot.services.ServiceManager;
import java.util.List;

public class UpdatesCommandTest {
    private final ServiceManager services;

    public UpdatesCommandTest(ServiceManager services) {
        this.services = services;
    }

    public void runUpdateScenarios() {
        System.out.println("\n--- 🆕 STARTING UPDATES COMMAND LOGIC SCENARIOS ---");

        // Scenario 1: Empty state (No updates in DB)
        testEmptyUpdates();

        // Scenario 2: Posting and retrieving multiple updates
        testUpdateRetrieval();

        // Scenario 3: Limit check (Should only get latest 5)
        testUpdateLimit();

        System.out.println("--- ✅ UPDATES COMMAND LOGIC COMPLETE ---");
    }

    private void testEmptyUpdates() {
        System.out.println("[1] Testing Empty Updates State...");
        // This assumes a fresh/clean test table
        List<UpdateAnnouncementEntity> updates = services.getUpdateAnnouncementService().getLatestUpdates(5);

        if (updates.isEmpty()) {
            System.out.println("    - PASSED: Correctly identified no updates.");
        } else {
            System.out.println("    - WARNING: Updates found. Ensure test database is clean.");
        }
    }

    private void testUpdateRetrieval() {
        System.out.println("[2] Testing Update Posting and Retrieval...");

        services.getUpdateAnnouncementService().postUpdate("Global Ranks", "Added global leaderboards!");
        services.getUpdateAnnouncementService().postUpdate("Anime Support", "Now featuring 500+ anime titles.");

        List<UpdateAnnouncementEntity> updates = services.getUpdateAnnouncementService().getLatestUpdates(5);

        System.out.println("    - Retrieved count: " + updates.size());
        boolean found = updates.stream().anyMatch(u -> u.getTitle().equals("Anime Support"));
        System.out.println("    - Content Verification: " + (found ? "SUCCESS" : "FAILURE"));
    }

    private void testUpdateLimit() {
        System.out.println("[3] Testing Retrieval Limit...");

        // Post many updates to exceed the limit of 5
        for (int i = 1; i <= 10; i++) {
            services.getUpdateAnnouncementService().postUpdate("Patch #" + i, "Minor fixes.");
        }

        List<UpdateAnnouncementEntity> updates = services.getUpdateAnnouncementService().getLatestUpdates(5);

        System.out.println("    - Requested 5, Received: " + updates.size());
        if (updates.size() == 5) {
            System.out.println("    - PASSED: Limit applied correctly.");
        } else {
            System.out.println("    - FAILED: Limit ignored.");
        }
    }
}
