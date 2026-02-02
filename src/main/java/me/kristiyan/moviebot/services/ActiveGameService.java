package me.kristiyan.moviebot.services;

import jakarta.persistence.criteria.*;
import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.database.entities.games.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class ActiveGameService {
    private final DatabaseManager databaseManager;

    public ActiveGameService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }


    // Check if a member has already guessed for a specific guild and round
    public boolean hasAlreadyGuessed(String guildId, String memberId, int roundNumber) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<ActiveGuessEntity> root = cq.from(ActiveGuessEntity.class);

            // Filter by guild, member, AND round
            Predicate predicate = cb.and(
                    cb.equal(root.get("guildId"), guildId),
                    cb.equal(root.get("memberId"), memberId),
                    cb.equal(root.get("roundNumber"), roundNumber)
            );

            cq.select(cb.count(root)).where(predicate);
            return session.createQuery(cq).getSingleResult() > 0;
        }
    }

    // Record the guess only if they haven't guessed yet
    public void recordGuessAttempt(String guildId, String memberId, int roundNumber) {
        if (hasAlreadyGuessed(guildId, memberId, roundNumber)) return;

        try (Session session = databaseManager.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            ActiveGuessEntity guess = new ActiveGuessEntity();
            guess.setGuildId(guildId);
            guess.setMemberId(memberId);
            guess.setRoundNumber(roundNumber);

            session.persist(guess);
            tx.commit();
        } catch (Exception e) {
            System.err.println("Error recording guess: " + e.getMessage());
        }
    }

    // Wipes all guesses for a guild (e.g., when a game ends)
    public void clearAllGuesses(String guildId) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaDelete<ActiveGuessEntity> cd = cb.createCriteriaDelete(ActiveGuessEntity.class);
            Root<ActiveGuessEntity> root = cd.from(ActiveGuessEntity.class);

            cd.where(cb.equal(root.get("guildId"), guildId));

            session.createMutationQuery(cd).executeUpdate();
            tx.commit();
        }
    }
}