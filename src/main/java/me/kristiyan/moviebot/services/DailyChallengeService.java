package me.kristiyan.moviebot.services;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.database.entities.games.DailyChallengeEntity;
import me.kristiyan.moviebot.database.entities.movie.MovieEntity;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Optional;

public class DailyChallengeService {
    private static final Logger logger = LoggerFactory.getLogger(DailyChallengeService.class);
    private final DatabaseManager databaseManager;

    public DailyChallengeService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Optional<MovieEntity> getTodayMovie() {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<DailyChallengeEntity> query = cb.createQuery(DailyChallengeEntity.class);
            Root<DailyChallengeEntity> root = query.from(DailyChallengeEntity.class);
            root.fetch("movie", JoinType.LEFT);
            query.select(root)
                    .where(cb.equal(root.get("challengeDate"), LocalDate.now()));
            DailyChallengeEntity challenge = session.createQuery(query).uniqueResult();
            if (challenge == null) {
                throw new RuntimeException("No daily challenge found for today: " + LocalDate.now());
            }
            return Optional.ofNullable(challenge.getMovie());
        } catch (Exception e) {
            logger.error(e.fillInStackTrace().toString());
            System.err.println(e.getMessage());
            return Optional.empty();
        }
    }
}
