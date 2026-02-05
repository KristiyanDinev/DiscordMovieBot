package me.kristiyan.moviebot.services;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.database.entities.games.ActiveGameEntity;
import me.kristiyan.moviebot.database.entities.games.ActiveGuessEntity;
import me.kristiyan.moviebot.database.entities.leaderboards.MemberStatusEntity;
import me.kristiyan.moviebot.enums.LeaderboardMemberStatus;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LeaderboardsService {
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
                    entity.setMemberNickname(entity.getMemberNickname());
                    entity.setGuildName(guildName);
                    entity.setStatusType(leaderboardMemberStatus.toString());
                    entity.setCorrectGuesses(pointsToAdd);
                } else {
                    entity.setCorrectGuesses(entity.getCorrectGuesses() + pointsToAdd);
                }

                session.merge(entity);
            }

            tx.commit();
        }
    }

    public List<MemberStatusEntity> getGuildLeaderboard(LeaderboardMemberStatus leaderboardMemberStatus, int limit) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();

            // 1. Subquery: Find the MAX(correctGuesses) for each guild
            // This identifies what the winning score is for every guild.
            CriteriaQuery<Integer> subquery = cb.createQuery(Integer.class);
            Root<MemberStatusEntity> subRoot = subquery.from(MemberStatusEntity.class);

            subquery.select(cb.max(subRoot.get("correctGuesses")))
                    .where(cb.equal(subRoot.get("statusType"), leaderboardMemberStatus.toString()))
                    .groupBy(subRoot.get("guildId"));

            // 2. Main Query: Fetch the entities that match those max scores
            CriteriaQuery<MemberStatusEntity> mainQuery = cb.createQuery(MemberStatusEntity.class);
            Root<MemberStatusEntity> root = mainQuery.from(MemberStatusEntity.class);

            // We filter by status and ensure the score is in the list of "maximums"
            mainQuery.select(root)
                    .where(
                            cb.equal(root.get("statusType"), leaderboardMemberStatus.toString()),
                            root.get("correctGuesses").in(subquery)
                    )
                    // Order by the score descending to get the actual "Top" guilds first
                    .orderBy(cb.desc(root.get("correctGuesses")));

            // 3. Apply the limit and handle duplicates
            // Note: If two users in one guild have the same max score, we use distinct or a stream limit
            List<MemberStatusEntity> results = session.createQuery(mainQuery)
                    .setMaxResults(limit)
                    .getResultList();

            // 4. De-duplicate in Java
            Map<String, MemberStatusEntity> uniqueGuilds = new LinkedHashMap<>();
            for (MemberStatusEntity entity : results) {
                // This prevents the same guild from appearing twice if 2 people have the top score
                uniqueGuilds.putIfAbsent(entity.getGuildId(), entity);
            }

            return new ArrayList<>(uniqueGuilds.values());
        }
    }

    private static <T> java.util.function.Predicate<T> distinctByKey(java.util.function.Function<? super T, ?> keyExtractor) {
        java.util.Set<Object> seen = java.util.concurrent.ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
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
        }
    }
}