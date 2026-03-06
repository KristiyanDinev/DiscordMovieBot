package me.kristiyan.moviebot.services;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.database.entities.games.ActiveGameEntity;
import me.kristiyan.moviebot.database.entities.games.ActiveGuessEntity;
import me.kristiyan.moviebot.database.entities.leaderboards.MemberStatusEntity;
import me.kristiyan.moviebot.enums.LeaderboardMemberStatus;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LeaderboardsService {
    private static final Logger logger = LoggerFactory.getLogger(LeaderboardsService.class);
    private final DatabaseManager databaseManager;
    private final ServiceManager serviceManager;

    public LeaderboardsService(DatabaseManager databaseManager, ServiceManager serviceManager) {
        this.databaseManager = databaseManager;
        this.serviceManager = serviceManager;
    }

    public void recordCorrectGuesses(String guildId, String guildName, LeaderboardMemberStatus leaderboardMemberStatus) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            ActiveGameEntity activeGameEntry = serviceManager.getActiveGameService().getActiveGameEntity(guildId);
            if (activeGameEntry == null) return;

            List<ActiveGuessEntity> correctGuesses = serviceManager.getActiveGameService().getActiveGuessEntities(activeGameEntry);
            if (correctGuesses.isEmpty()) return;

            // 1. AGGREGATE scores first: Map<MemberId, PointsWon>
            // This handles the "2 same IDs" scenario perfectly.
            Map<String, Long> pointsPerMember = correctGuesses.stream()
                    .collect(Collectors.groupingBy(ActiveGuessEntity::getMemberId, Collectors.counting()));

            List<String> memberIds = new ArrayList<>(pointsPerMember.keySet());

            // 2. Fetch existing records in one trip
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<MemberStatusEntity> query = cb.createQuery(MemberStatusEntity.class);
            Root<MemberStatusEntity> root = query.from(MemberStatusEntity.class);

            query.select(root).where(
                    cb.equal(root.get("guildId"), guildId),
                    root.get("memberId").in(memberIds),
                    cb.equal(root.get("statusType"), leaderboardMemberStatus.toString())
            );

            Map<String, MemberStatusEntity> existingStats = session.createQuery(query)
                    .getResultStream()
                    .collect(Collectors.toMap(MemberStatusEntity::getMemberId, s -> s));

            Map<String, String> idToNickname = correctGuesses.stream()
                    .collect(Collectors.toMap(
                            ActiveGuessEntity::getMemberId,
                            ActiveGuessEntity::getMemberNickname,
                            (existing, replacement) -> existing // Keep the first one found if duplicates exist
                    ));

            // 3. Process each UNIQUE member
            Transaction tx = session.beginTransaction();

            for (Map.Entry<String, Long> entry : pointsPerMember.entrySet()) {
                String mId = entry.getKey();
                int pointsToAdd = entry.getValue().intValue();
                MemberStatusEntity entity = existingStats.get(mId);

                if (entity == null) {
                    entity = new MemberStatusEntity();
                    entity.setGuildId(guildId);
                    entity.setMemberId(mId);
                    entity.setMemberNickname(idToNickname.getOrDefault(mId, "Unknown User"));
                    entity.setGuildName(guildName);
                    entity.setStatusType(leaderboardMemberStatus.toString());
                    entity.setCorrectGuesses(pointsToAdd);
                } else {
                    entity.setCorrectGuesses(entity.getCorrectGuesses() + pointsToAdd);
                }

                session.merge(entity);
            }

            tx.commit();
        } catch (Exception ex) {
            logger.error(ex.fillInStackTrace().toString());
            System.err.println(ex.getMessage());
        }
    }

    public List<MemberStatusEntity> getGuildLeaderboard(LeaderboardMemberStatus leaderboardMemberStatus, int limit) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();

            // Use Tuple to handle multiple selection in a type-safe way
            CriteriaQuery<Tuple> cq = cb.createTupleQuery();
            Root<MemberStatusEntity> root = cq.from(MemberStatusEntity.class);

            // Define the selection using an array-like structure via tuple()
            cq.select(cb.tuple(
                    root.get("guildId").alias("id"),
                    root.get("guildName").alias("name"),
                    cb.sum(root.get("correctGuesses")).alias("total")
            ));

            cq.where(cb.equal(root.get("statusType"), leaderboardMemberStatus.toString()));
            cq.groupBy(root.get("guildId"), root.get("guildName"));
            cq.orderBy(cb.desc(cb.sum(root.get("correctGuesses"))));


            return session.createQuery(cq)
                    .setMaxResults(limit)
                    .getResultList().stream().map(tuple -> {
                MemberStatusEntity entity = new MemberStatusEntity();
                entity.setGuildId(tuple.get("id", String.class));
                entity.setGuildName(tuple.get("name", String.class));
                // Safely handle the Sum result (usually returns Long or Number)
                Number sum = tuple.get("total", Number.class);
                entity.setCorrectGuesses(sum != null ? sum.intValue() : 0);
                entity.setStatusType(leaderboardMemberStatus.toString());
                return entity;
            }).collect(Collectors.toList());

        } catch (Exception ex) {
            logger.error("Error fetching guild leaderboard: ", ex);
            return List.of();
        }
    }


    public List<MemberStatusEntity> getMemberLeaderboard(String guildId, int limit, LeaderboardMemberStatus status) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<MemberStatusEntity> cq = cb.createQuery(MemberStatusEntity.class);
            Root<MemberStatusEntity> root = cq.from(MemberStatusEntity.class);
            cq.select(root)
                    .where(
                            cb.equal(root.get("guildId"), guildId),
                            cb.equal(root.get("statusType"), status.toString())
                    )
                    .orderBy(
                            cb.desc(root.get("correctGuesses")),
                            cb.asc(root.get("memberId"))
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

    public List<MemberStatusEntity> getDailyMemberLeaderboard(String guildId, int limit, LeaderboardMemberStatus status) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<MemberStatusEntity> cq = cb.createQuery(MemberStatusEntity.class);
            Root<MemberStatusEntity> root = cq.from(MemberStatusEntity.class);

            // Define the daily filter using the 'guessedAt' field
            Predicate isToday = cb.equal(root.get("guessedAt"), cb.currentDate());

            cq.select(root)
                    .where(
                            cb.equal(root.get("guildId"), guildId),
                            cb.equal(root.get("statusType"), status.toString()),
                            isToday // Added the daily constraint here
                    )
                    .orderBy(
                            cb.desc(root.get("correctGuesses")),
                            cb.asc(root.get("memberId"))
                    );

            return session.createQuery(cq)
                    .setMaxResults(limit)
                    .getResultList();
        } catch (Exception ex) {
            logger.error("Error fetching daily member leaderboard: ", ex);
            return List.of();
        }
    }

    public List<MemberStatusEntity> getDailyGuildLeaderboard(LeaderboardMemberStatus status, int limit) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Tuple> cq = cb.createTupleQuery();
            Root<MemberStatusEntity> root = cq.from(MemberStatusEntity.class);

            // 1. Define the Date Filter (Filtering for today's records)
            // Note: Replace "createdAt" with your actual date/timestamp column name
            Predicate dailyPredicate = cb.equal(
                    cb.function("date", java.sql.Date.class, root.get("guessedAt")),
                    cb.currentDate()
            );

            cq.select(cb.tuple(
                    root.get("guildId").alias("id"),
                    root.get("guildName").alias("name"),
                    cb.sum(root.get("correctGuesses")).alias("total")
            ));

            // 2. Combine the status check and the date check
            cq.where(cb.and(
                    cb.equal(root.get("statusType"), status.toString()),
                    dailyPredicate
            ));

            cq.groupBy(root.get("guildId"), root.get("guildName"));
            cq.orderBy(cb.desc(cb.sum(root.get("correctGuesses"))));

            return session.createQuery(cq)
                    .setMaxResults(limit)
                    .getResultList().stream().map(tuple -> {
                        MemberStatusEntity entity = new MemberStatusEntity();
                        entity.setGuildId(tuple.get("id", String.class));
                        entity.setGuildName(tuple.get("name", String.class));

                        Number sum = tuple.get("total", Number.class);
                        entity.setCorrectGuesses(sum != null ? sum.intValue() : 0);
                        entity.setStatusType(status.toString());
                        return entity;
                    }).collect(Collectors.toList());

        } catch (Exception ex) {
            logger.error("Error fetching daily guild leaderboard: ", ex);
            return List.of();
        }
    }
}