package me.kristiyan.moviebot.services;

import jakarta.persistence.criteria.*;
import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.database.entities.premium.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
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

    // Activate a premium code for a user
    public boolean activateCode(String userId, String codeString, int months) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            PremiumCodeEntity code = session.find(PremiumCodeEntity.class, codeString);
            if (code == null) return false;

            PremiumUserEntity user = session.find(PremiumUserEntity.class, userId);
            if (user == null) {
                user = new PremiumUserEntity();
                user.setUserId(userId);
                user.setExpiresAt(LocalDateTime.now());
            }

            user.setExpiresAt(user.getExpiresAt().plusMonths(months));
            user.setActivationCode(codeString);

            session.remove(code);
            session.merge(user);

            tx.commit();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Add a new premium code to the database
    public void generateCode(String code) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            PremiumCodeEntity newCode = new PremiumCodeEntity();
            newCode.setCode(code);

            session.persist(newCode);
            tx.commit();
        }
    }
}