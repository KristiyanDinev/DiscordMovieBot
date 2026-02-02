package me.kristiyan.moviebot.services;

import jakarta.persistence.criteria.*;
import me.kristiyan.moviebot.database.entities.leaderboards.GuildLeaderboardEntity;
import me.kristiyan.moviebot.database.entities.leaderboards.MemberStatusEntity;
import me.kristiyan.moviebot.database.DatabaseManager;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class LeaderboardsService {
    private final DatabaseManager databaseManager;

    public LeaderboardsService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }


    public void recordCorrectGuess(String guildId, String guildName, String memberId, String memberNickname) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                GuildLeaderboardEntity guildEntry = session.find(GuildLeaderboardEntity.class, guildId);
                if (guildEntry == null) {
                    guildEntry = new GuildLeaderboardEntity();
                    guildEntry.setGuildId(guildId);
                    guildEntry.setCorrectGuesses(0);
                }
                guildEntry.setGuildName(guildName);
                guildEntry.setCorrectGuesses(guildEntry.getCorrectGuesses() + 1);
                session.merge(guildEntry);

                CriteriaBuilder cb = session.getCriteriaBuilder();
                CriteriaQuery<MemberStatusEntity> cq = cb.createQuery(MemberStatusEntity.class);
                Root<MemberStatusEntity> root = cq.from(MemberStatusEntity.class);

                cq.where(cb.and(
                        cb.equal(root.get("guildId"), guildId),
                        cb.equal(root.get("memberId"), memberId)
                ));

                MemberStatusEntity memberEntry = session.createQuery(cq).uniqueResultOptional().orElse(null);

                if (memberEntry == null) {
                    memberEntry = new MemberStatusEntity();
                    memberEntry.setGuildId(guildId);
                    memberEntry.setMemberId(memberId);
                    memberEntry.setCorrectGuesses(0);
                }
                memberEntry.setMemberNickname(memberNickname);
                memberEntry.setCorrectGuesses(memberEntry.getCorrectGuesses() + 1);
                session.merge(memberEntry);

                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    public List<GuildLeaderboardEntity> getGuildLeaderboard(int limit) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<GuildLeaderboardEntity> cq = cb.createQuery(GuildLeaderboardEntity.class);
            Root<GuildLeaderboardEntity> root = cq.from(GuildLeaderboardEntity.class);

            cq.select(root).orderBy(cb.desc(root.get("correctGuesses")));

            return session.createQuery(cq)
                    .setMaxResults(limit)
                    .getResultList();
        }
    }

    public List<MemberStatusEntity> getMemberLeaderboard(String guildId, int limit) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<MemberStatusEntity> cq = cb.createQuery(MemberStatusEntity.class);
            Root<MemberStatusEntity> root = cq.from(MemberStatusEntity.class);

            cq.select(root)
                    .where(cb.equal(root.get("guildId"), guildId))
                    .orderBy(cb.desc(root.get("correctGuesses")));

            return session.createQuery(cq)
                    .setMaxResults(limit)
                    .getResultList();
        }
    }
}