package me.kristiyan.moviebot.services;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.database.entities.leaderboards.MemberGenreKey;
import me.kristiyan.moviebot.database.entities.movie.MovieEntity;
import me.kristiyan.moviebot.database.entities.movie.MovieGenreEntity;
import me.kristiyan.moviebot.database.entities.leaderboards.MemberGenreCountEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class MovieService {
    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);
    private final DatabaseManager databaseManager;

    public MovieService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    // Increment per-member genre counters for the given movie.
    // This is a simple read-then-update approach (transactional). For high concurrency,
    // prefer a DB upsert approach (native query with ON CONFLICT) implemented in LeaderboardsService.
    public boolean incrementMemberGenreCounts(String memberId, int movieId, int incrementBy) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                MovieEntity managedMovie = session.find(MovieEntity.class, movieId);
                if (managedMovie == null) {
                    tx.rollback();
                    return false;
                }

                for (MovieGenreEntity mg : managedMovie.getGenres()) {
                    String genre = mg.getGenre();
                    MemberGenreCountEntity existing = session.find(MemberGenreCountEntity.class,
                            new MemberGenreKey(memberId, genre));
                    if (existing != null) {
                        existing.increment(incrementBy);
                        session.merge(existing);
                    } else {
                        MemberGenreCountEntity created = new MemberGenreCountEntity(memberId, genre, incrementBy);
                        session.persist(created);
                    }
                }

                tx.commit();
                return true;
            } catch (Exception e) {
                tx.rollback();
                logger.error(e.fillInStackTrace().toString());
                System.err.println(e.getMessage());
                return false;
            }
        } catch (Exception e) {
            logger.error(e.fillInStackTrace().toString());
            System.err.println(e.getMessage());
            return false;
        }
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
        } catch (Exception e) {
            logger.error(e.fillInStackTrace().toString());
            System.err.println(e.getMessage());
            return 0;
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
                logger.error(e.fillInStackTrace().toString());
                System.err.println(e.getMessage());
                return false;
            }
        } catch (Exception e) {
            logger.error(e.fillInStackTrace().toString());
            System.err.println(e.getMessage());
            return false;
        }
    }

    // Get random of TYPE
    public Optional<MovieEntity> getRandomMovie(String type) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<MovieEntity> cq = cb.createQuery(MovieEntity.class);
            Root<MovieEntity> root = cq.from(MovieEntity.class);

            cq.select(root)
                    .where(cb.equal(root.get("contentType"), type))
                    .orderBy(cb.asc(cb.function("random", Double.class)));

            return Optional.ofNullable(session.createQuery(cq)
                    .setMaxResults(1)
                    .uniqueResult());
        } catch (Exception ex) {
            logger.error(ex.fillInStackTrace().toString());
            System.err.println(ex.getMessage());
            return Optional.empty();
        }
    }
}