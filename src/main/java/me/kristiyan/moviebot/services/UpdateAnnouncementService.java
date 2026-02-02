package me.kristiyan.moviebot.services;

import jakarta.persistence.criteria.*;
import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.database.entities.updates.UpdateAnnouncementEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class UpdateAnnouncementService {
    private final DatabaseManager databaseManager;

    public UpdateAnnouncementService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    // Post a new update
    public void postUpdate(String title, String description) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            UpdateAnnouncementEntity update = new UpdateAnnouncementEntity();
            update.setTitle(title);
            update.setShortDescription(description);
            // date is handled by the entity default: LocalDateTime.now()
            session.persist(update);
            tx.commit();
        }
    }

    // Get the latest X updates
    public List<UpdateAnnouncementEntity> getLatestUpdates(int limit) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<UpdateAnnouncementEntity> cq = cb.createQuery(UpdateAnnouncementEntity.class);
            Root<UpdateAnnouncementEntity> root = cq.from(UpdateAnnouncementEntity.class);

            // Order by date descending
            cq.select(root).orderBy(cb.desc(root.get("updateReleasedAt")));
            return session.createQuery(cq)
                    .setMaxResults(limit)
                    .getResultList();
        }
    }
}