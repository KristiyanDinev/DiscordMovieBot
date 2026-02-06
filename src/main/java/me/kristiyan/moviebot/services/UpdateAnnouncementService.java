package me.kristiyan.moviebot.services;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.database.entities.updates.UpdateAnnouncementEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UpdateAnnouncementService {
    private static final Logger logger = LoggerFactory.getLogger(UpdateAnnouncementService.class);
    private final DatabaseManager databaseManager;

    public UpdateAnnouncementService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void postUpdate(String title, String description) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                UpdateAnnouncementEntity update = new UpdateAnnouncementEntity();
                update.setTitle(title);
                update.setShortDescription(description);

                session.persist(update);
                tx.commit();
            } catch (Exception ex) {
                if (tx != null) tx.rollback();
                logger.error(ex.fillInStackTrace().toString());
                System.err.println(ex.getMessage());
            }
        }
    }

    public List<UpdateAnnouncementEntity> getLatestUpdates(int limit) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<UpdateAnnouncementEntity> cq = cb.createQuery(UpdateAnnouncementEntity.class);
            Root<UpdateAnnouncementEntity> root = cq.from(UpdateAnnouncementEntity.class);

            // FIX: Use the Java field name "releasedAt", not the DB column name
            // ADDED: Secondary sort by ID to ensure order if multiple updates happen same day
            cq.select(root).orderBy(
                    cb.desc(root.get("releasedAt")),
                    cb.desc(root.get("id"))
            );

            return session.createQuery(cq)
                    .setMaxResults(limit)
                    .getResultList();

        } catch (Exception ex) {
            logger.error(ex.fillInStackTrace().toString());
            System.err.println(ex.getMessage());
            return List.of();
        }
    }
}