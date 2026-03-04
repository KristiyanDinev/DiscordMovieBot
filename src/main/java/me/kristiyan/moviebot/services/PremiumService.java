package me.kristiyan.moviebot.services;

import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.database.entities.premium.PremiumUserEntity;
import org.hibernate.Session;

import java.time.LocalDateTime;

public class PremiumService {
    private final DatabaseManager databaseManager;

    public PremiumService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    // Check if a user is currently premium
    public boolean isPremium(String userId) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            PremiumUserEntity user = session.find(PremiumUserEntity.class, userId);
            if (user == null) return false;
            return user.getExpiresAt().isAfter(LocalDateTime.now());
        }
    }
}