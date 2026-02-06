package me.kristiyan.moviebot.services;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.database.entities.movie.MovieEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovieService {
    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);
    private final DatabaseManager databaseManager;

    public MovieService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    // Get total movie count using Criteria API
    public int getMovieCount() {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<MovieEntity> root = cq.from(MovieEntity.class);
            cq.select(cb.count(root));

            Long count = session.createQuery(cq).getSingleResult();
            return count != null ? count.intValue() : 0;
        }
    }

    // Add movie using Object-Oriented Persist
    public boolean addMovie(String title, String gifUrl) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<MovieEntity> root = cq.from(MovieEntity.class);
            cq.select(cb.count(root)).where(cb.equal(root.get("title"), title));

            if (session.createQuery(cq).getSingleResult() > 0) {
                return false; // Already exists
            }

            Transaction tx = session.beginTransaction();
            try {
                MovieEntity movie = new MovieEntity();
                movie.setTitle(title);
                movie.setGifUrl(gifUrl);

                session.persist(movie);
                tx.commit();
                return true;
            } catch (Exception e) {
                tx.rollback();
                return false;
            }
        }
    }

    // Get random movie or anime
    public MovieEntity getRandomMovie() {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<MovieEntity> cq = cb.createQuery(MovieEntity.class);
            Root<MovieEntity> root = cq.from(MovieEntity.class);

            // Using the CriteriaBuilder to call the 'random' function
            cq.select(root).orderBy(cb.asc(cb.function("random", Double.class)));

            return session.createQuery(cq)
                    .setMaxResults(1)
                    .uniqueResult();
        }
    }

    // Get random of TYPE
    public MovieEntity getRandomMovie(String type) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<MovieEntity> cq = cb.createQuery(MovieEntity.class);
            Root<MovieEntity> root = cq.from(MovieEntity.class);

            cq.select(root)
                    .where(cb.equal(root.get("contentType"), type))
                    .orderBy(cb.asc(cb.function("random", Double.class)));

            return session.createQuery(cq)
                    .setMaxResults(1)
                    .uniqueResult();
        } catch (Exception ex) {
            logger.error(ex.fillInStackTrace().toString());
            System.err.println(ex.getMessage());
            return null;
        }
    }
}