package me.kristiyan.moviebot.services;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.database.entities.games.ActiveGameEntity;
import me.kristiyan.moviebot.database.entities.games.ActiveGuessEntity;
import me.kristiyan.moviebot.database.entities.movie.MovieEntity;
import me.kristiyan.moviebot.enums.GamemodeEnum;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class ActiveGameService {
    private static final Logger logger = LoggerFactory.getLogger(ActiveGameService.class);
    private final DatabaseManager databaseManager;

    public ActiveGameService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }


    public boolean startGame(String guildId, MovieEntity movieEntity, int totalRounds,
                             boolean highStakes, boolean locked, String memberId, GamemodeEnum gamemode) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            ActiveGameEntity activeGameEntity = new ActiveGameEntity();
            activeGameEntity.setGuildId(guildId);
            activeGameEntity.setMovie(movieEntity);
            activeGameEntity.setTotalRounds(totalRounds);
            activeGameEntity.setHighStake(highStakes);
            activeGameEntity.setLocked(locked);
            activeGameEntity.setLockedMemberId(locked ? memberId : null);
            activeGameEntity.setGamemode(gamemode);
            session.persist(activeGameEntity);
            tx.commit();
            return true;

        } catch (Exception ex) {
            logger.error(ex.fillInStackTrace().toString());
            System.err.println(ex.getMessage());
            return false;
        }
    }

    /**
     * Finds the game relevant to the specific user.
     * Priority: 1. Their own Locked Game. 2. The Guild's Public Game.
     */
    @Nullable
    public ActiveGameEntity findRelevantGame(String guildId, String memberId, GamemodeEnum gamemode) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ActiveGameEntity> cq = cb.createQuery(ActiveGameEntity.class);
            Root<ActiveGameEntity> root = cq.from(ActiveGameEntity.class);

            // Logic: (guildId = G AND lockedMemberId = M) OR (guildId = G AND lockedMemberId IS NULL)
            cq.where(cb.or(cb.and(
                    cb.equal(root.get("guildId"), guildId),
                    cb.equal(root.get("gamemode"), gamemode),
                    cb.equal(root.get("lockedMemberId"), memberId)
            ), cb.and(
                    cb.equal(root.get("guildId"), guildId),
                    cb.equal(root.get("gamemode"), gamemode),
                    cb.isNull(root.get("lockedMemberId"))
            )));

            // Order by lockedMemberId DESC (Non-nulls come before Nulls in standard SQL sorting usually,
            // but effectively we want to catch the private game if it exists)
            // Safer: Order by 'locked' descending (true before false)
            cq.orderBy(cb.desc(root.get("locked")));

            return session.createQuery(cq).setMaxResults(1).uniqueResult();

        }  catch (Exception ex) {
            logger.error(ex.fillInStackTrace().toString());
            System.err.println(ex.getMessage());
            return null;
        }
    }

    public Optional<ActiveGameEntity> getActiveGameEntity(String guildId, GamemodeEnum gamemode) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ActiveGameEntity> cq = cb.createQuery(ActiveGameEntity.class);
            Root<ActiveGameEntity> root = cq.from(ActiveGameEntity.class);
            cq.select(root).where(cb.equal(root.get("guildId"), guildId), cb.equal(root.get("gamemode"), gamemode));
            return Optional.ofNullable(session.createQuery(cq).setMaxResults(1).uniqueResult());
        }  catch (Exception ex) {
            logger.error(ex.fillInStackTrace().toString());
            System.err.println(ex.getMessage());
            return Optional.empty();
        }
    }

    // Record the guess only if they haven't guessed yet
    public void recordGuessAttempt(String guildId, String memberId, String memberNickname,
                                   int roundNumber, ActiveGameEntity game) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            ActiveGuessEntity guess = new ActiveGuessEntity();
            guess.setGuildId(guildId);
            guess.setMemberId(memberId);
            guess.setRoundNumber(roundNumber);
            guess.setActiveGame(game);
            guess.setMemberNickname(memberNickname);
            session.persist(guess);
            tx.commit();
        } catch (Exception ex) {
            logger.error(ex.fillInStackTrace().toString());
            System.err.println(ex.getMessage());
        }
    }

    public void endGame(ActiveGameEntity game) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                CriteriaBuilder cb = session.getCriteriaBuilder();

                // 1. Delete guesses ONLY for this specific game instance
                CriteriaDelete<ActiveGuessEntity> deleteGuesses = cb.createCriteriaDelete(ActiveGuessEntity.class);
                Root<ActiveGuessEntity> guessRoot = deleteGuesses.from(ActiveGuessEntity.class);
                deleteGuesses.where(cb.equal(guessRoot.get("activeGame"), game));
                session.createMutationQuery(deleteGuesses).executeUpdate();

                // 2. Delete the game itself
                session.remove(session.contains(game) ? game : session.merge(game));
                tx.commit();

            } catch (Exception ex) {
                if (tx != null) tx.rollback();
                logger.error(ex.fillInStackTrace().toString());
                System.err.println(ex.getMessage());
            }
        }
    }

    public List<ActiveGuessEntity> getActiveGuessEntities(ActiveGameEntity game) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ActiveGuessEntity> cq = cb.createQuery(ActiveGuessEntity.class);
            Root<ActiveGuessEntity> root = cq.from(ActiveGuessEntity.class);

            // Filter by the specific game object
            cq.select(root).where(cb.equal(root.get("activeGame"), game));

            return session.createQuery(cq).getResultList();
        }  catch (Exception ex) {
            logger.error("Error getting guesses", ex);
            return List.of();
        }
    }

    public void updateActiveGame(ActiveGameEntity entity) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.merge(entity);
                tx.commit();
            } catch (Exception ex) {
                if (tx != null) tx.rollback();
                logger.error(ex.fillInStackTrace().toString());
                System.err.println(ex.getMessage());
            }
        }
    }

    public boolean hasActivePublicGame(String guildId, GamemodeEnum gamemode) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<ActiveGameEntity> root = cq.from(ActiveGameEntity.class);

            cq.select(cb.count(root));
            cq.where(
                    cb.equal(root.get("guildId"), guildId),
                    cb.equal(root.get("gamemode"), gamemode),
                    cb.isNull(root.get("lockedMemberId")) // Public = No locked member
            );

            return session.createQuery(cq).getSingleResult() > 0;
        }
    }

    public boolean hasActiveLockedGame(String guildId, String memberId) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<ActiveGameEntity> root = cq.from(ActiveGameEntity.class);

            cq.select(cb.count(root));
            cq.where(
                    cb.equal(root.get("guildId"), guildId),
                    cb.equal(root.get("lockedMemberId"), memberId)
            );

            return session.createQuery(cq).getSingleResult() > 0;
        } catch (Exception ex) {
            logger.error(ex.fillInStackTrace().toString());
            System.err.println(ex.getMessage());
            return false;
        }
    }
}