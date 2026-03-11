package me.kristiyan.moviebot.services;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.database.dto.GuildLeaderboardDTO;
import me.kristiyan.moviebot.database.dto.MemberLeaderboardDTO;
import me.kristiyan.moviebot.database.entities.games.ActiveGameEntity;
import me.kristiyan.moviebot.database.entities.games.ActiveGuessEntity;
import me.kristiyan.moviebot.database.entities.guild.GuildEntity;
import me.kristiyan.moviebot.database.entities.guild.GuildMemberEntity;
import me.kristiyan.moviebot.database.entities.guild.MemberEntity;
import me.kristiyan.moviebot.database.entities.leaderboards.MemberStatusEntity;
import me.kristiyan.moviebot.enums.GamemodeEnum;
import me.kristiyan.moviebot.enums.LeaderboardMemberStatusEnum;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardsService {
    private static final Logger logger = LoggerFactory.getLogger(LeaderboardsService.class);
    private final DatabaseManager databaseManager;
    private final ServiceManager serviceManager;

    public LeaderboardsService(DatabaseManager databaseManager, ServiceManager serviceManager) {
        this.databaseManager = databaseManager;
        this.serviceManager = serviceManager;
    }

    public void recordCorrectGuesses(String guildId, String guildName,
                                     LeaderboardMemberStatusEnum leaderboardMemberStatusEnum,
                                     GamemodeEnum gamemodeEnum) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            Optional<ActiveGameEntity> activeGameEntryOptional = serviceManager.getActiveGameService()
                    .getActiveGameEntity(guildId, gamemodeEnum);
            if (activeGameEntryOptional.isEmpty()) return;
            ActiveGameEntity activeGameEntry = activeGameEntryOptional.get();
            List<ActiveGuessEntity> correctGuesses = serviceManager.getActiveGameService()
                    .getActiveGuessEntities(activeGameEntry);
            if (correctGuesses.isEmpty()) return;

            Transaction tx = session.beginTransaction();
            try {
                // Map<memberId , Value>
                Map<String, Long> guessesPerMember = aggregateGuessesPerMember(correctGuesses);
                // Creates or updates database objects if they don't exist
                GuildEntity guild = upsertGuild(session, guildId, guildName);

                Map<String, MemberEntity> memberEntities = upsertMembers(session,
                        extractLatestNames(correctGuesses), guessesPerMember);
                Map<String, GuildMemberEntity> guildMembers = upsertGuildMembers(
                        session, guild, memberEntities);
                // Flush to ensure GuildMember IDs exist before creating daily stats
                session.flush();
                // Updates the leaderboard
                upsertMemberDailyStats(session, guildMembers, guessesPerMember, leaderboardMemberStatusEnum);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                logger.error(e.fillInStackTrace().toString());
                System.err.println(e.getMessage());
            }
        } catch (Exception e) {
            logger.error(e.fillInStackTrace().toString());
            System.err.println(e.getMessage());
        }
    }

    private Map<String, Long> aggregateGuessesPerMember(List<ActiveGuessEntity> guesses) {
        return guesses.stream()
                .collect(Collectors.groupingBy(ActiveGuessEntity::getMemberId, Collectors.counting()));
    }

    private Map<String, String> extractLatestNames(List<ActiveGuessEntity> guesses) {
        return guesses.stream()
                .collect(Collectors.toMap(
                        ActiveGuessEntity::getMemberId,
                        ActiveGuessEntity::getMemberNickname,
                        (existing, replacement) -> existing // Or 'replacement' if order guarantees latest
                ));
    }

    private GuildEntity upsertGuild(Session session, String guildId, String guildName) {
        GuildEntity guild = session.find(GuildEntity.class, guildId);
        if (guild == null) {
            guild = new GuildEntity();
            guild.setGuildId(guildId);
            guild.setGuildName(guildName);
            session.persist(guild);
        } else if (!guild.getGuildName().equals(guildName)) {
            guild.setGuildName(guildName);
            session.merge(guild);
        }
        return guild;
    }

    private Map<String, MemberEntity> upsertMembers(Session session, Map<String, String> idToNames,
                                                    Map<String, Long> guessesPerMember) {
        Map<String, MemberEntity> memberEntities = new HashMap<>();
        for (Map.Entry<String, String> entry : idToNames.entrySet()) {
            String memberId = entry.getKey();
            Long xp = guessesPerMember.get(memberId);
            if (xp == null) continue;
            String name = entry.getValue();
            MemberEntity memberEntity = session.find(MemberEntity.class, memberId);
            if (memberEntity == null) {
                memberEntity = new MemberEntity();
                memberEntity.setMemberId(memberId);
                memberEntity.setMemberName(name != null ? name : "Unknown User");
                memberEntity.setSubscribedAt(null);
                memberEntity.setExpiresAt(null);
                session.persist(memberEntity);

            } else if (!memberEntity.getMemberName().equals(name)) {
                memberEntity.setMemberName(name);
                session.merge(memberEntity);
            }
            memberEntities.put(memberId, memberEntity);
        }
        return memberEntities;
    }

    private Map<String, GuildMemberEntity> upsertGuildMembers(
            Session session, GuildEntity guild, Map<String, MemberEntity> members) {

        Map<String, GuildMemberEntity> updatedGuildMembers = new HashMap<>();
        if (members.isEmpty()) return updatedGuildMembers;

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GuildMemberEntity> query = cb.createQuery(GuildMemberEntity.class);
        Root<GuildMemberEntity> root = query.from(GuildMemberEntity.class);
        Set<String> memberIds = members.keySet();
        query.select(root).where(
                cb.equal(root.get("guild").get("guildId"), guild.getGuildId()),
                root.get("member").get("memberId").in(memberIds)
        );
        Map<String, GuildMemberEntity> existingMembers = session.createQuery(query).getResultList().stream()
                .collect(Collectors.toMap(m -> m.getMember().getMemberId(), m -> m));

        for (String memberId : memberIds) {
            GuildMemberEntity guildMember = existingMembers.get(memberId);
            if (guildMember == null) {
                guildMember = new GuildMemberEntity();
                guildMember.setGuild(guild);
                guildMember.setMember(members.get(memberId));
                session.persist(guildMember);
            }
            updatedGuildMembers.put(memberId, guildMember);
        }
        return updatedGuildMembers;
    }

    private void upsertMemberDailyStats(
            Session session,
            Map<String, GuildMemberEntity> guildMembers,
            Map<String, Long> guessesPerMember,
            LeaderboardMemberStatusEnum status) {
        LocalDate today = LocalDate.now();
        // Not member Ids, but row Ids
        List<Long> guildMemberIds = guildMembers.values().stream()
                .map(GuildMemberEntity::getId)
                .collect(Collectors.toList());

        if (guildMemberIds.isEmpty()) return;

        // --- Replaced String Query with Criteria API ---
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MemberStatusEntity> query = cb.createQuery(MemberStatusEntity.class);
        Root<MemberStatusEntity> root = query.from(MemberStatusEntity.class);

        query.select(root).where(
                root.get("guildMember").get("id").in(guildMemberIds),
                cb.equal(root.get("statusType"), status),
                cb.equal(root.get("guessedAt"), today)
        );

        Map<Long, MemberStatusEntity> existingStats = session.createQuery(query).getResultList().stream()
                .collect(Collectors.toMap(s -> s.getGuildMember().getId(), s -> s));

        for (Map.Entry<String, Long> entry : guessesPerMember.entrySet()) {
            String memberId = entry.getKey();
            int correctGuessesToAdd = entry.getValue().intValue();
            GuildMemberEntity gm = guildMembers.get(memberId);
            if (gm == null) continue;

            MemberStatusEntity stat = existingStats.get(gm.getId());
            if (stat == null) {
                stat = new MemberStatusEntity();
                stat.setGuildMember(gm);
                stat.setStatusType(status);
                stat.setGuessedAt(today);
                stat.setCorrectGuesses(correctGuessesToAdd);
                session.persist(stat);
            } else {
                stat.setCorrectGuesses(stat.getCorrectGuesses() + correctGuessesToAdd);
                session.merge(stat);
            }
        }
    }



    /**
     * Get guild leaderboard by aggregating stats across all members in each guild
     */
    public List<GuildLeaderboardDTO> getGuildLeaderboard(LeaderboardMemberStatusEnum leaderboardMemberStatusEnum, int limit) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Tuple> cq = cb.createTupleQuery();

            Root<MemberStatusEntity> statsRoot = cq.from(MemberStatusEntity.class);
            Join<MemberStatusEntity, GuildMemberEntity> memberJoin = statsRoot.join("guildMember");
            Join<GuildMemberEntity, GuildEntity> guildJoin = memberJoin.join("guild");

            cq.select(cb.tuple(
                    guildJoin.get("guildId").alias("guildId"),
                    guildJoin.get("guildName").alias("guildName"),
                    cb.sum(statsRoot.get("correctGuesses")).alias("totalGuesses")
            ));

            cq.where(cb.equal(statsRoot.get("statusType"), leaderboardMemberStatusEnum));

            cq.groupBy(guildJoin.get("guildId"), guildJoin.get("guildName"));
            cq.orderBy(cb.desc(cb.sum(statsRoot.get("correctGuesses"))));

            return session.createQuery(cq)
                    .setMaxResults(limit)
                    .getResultList().stream()
                    .map(tuple -> {
                        GuildLeaderboardDTO dto = new GuildLeaderboardDTO();
                        dto.setGuildId(tuple.get("guildId", String.class));
                        dto.setGuildName(tuple.get("guildName", String.class));
                        Number sum = tuple.get("totalGuesses", Number.class);
                        dto.setTotalCorrectGuesses(sum != null ? sum.intValue() : 0);
                        dto.setStatusType(leaderboardMemberStatusEnum);
                        return dto;
                    })
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            logger.error("Error fetching guild leaderboard: ", ex);
            return List.of();
        }
    }

    /**
     * Get daily guild leaderboard for today's stats only
     */
    public List<GuildLeaderboardDTO> getDailyGuildLeaderboard(LeaderboardMemberStatusEnum status, int limit) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Tuple> cq = cb.createTupleQuery();

            Root<MemberStatusEntity> statsRoot = cq.from(MemberStatusEntity.class);
            Join<MemberStatusEntity, GuildMemberEntity> memberJoin = statsRoot.join("guildMember");
            Join<GuildMemberEntity, GuildEntity> guildJoin = memberJoin.join("guild");

            LocalDate today = LocalDate.now();

            cq.select(cb.tuple(
                    guildJoin.get("guildId").alias("guildId"),
                    guildJoin.get("guildName").alias("guildName"),
                    cb.sum(statsRoot.get("correctGuesses")).alias("totalGuesses")
            ));

            cq.where(cb.and(
                    cb.equal(statsRoot.get("statusType"), status),
                    cb.equal(statsRoot.get("guessedAt"), today)
            ));

            cq.groupBy(guildJoin.get("guildId"), guildJoin.get("guildName"));
            cq.orderBy(cb.desc(cb.sum(statsRoot.get("correctGuesses"))));

            return session.createQuery(cq)
                    .setMaxResults(limit)
                    .getResultList().stream()
                    .map(tuple -> {
                        GuildLeaderboardDTO dto = new GuildLeaderboardDTO();
                        dto.setGuildId(tuple.get("guildId", String.class));
                        dto.setGuildName(tuple.get("guildName", String.class));
                        Number sum = tuple.get("totalGuesses", Number.class);
                        dto.setTotalCorrectGuesses(sum != null ? sum.intValue() : 0);
                        dto.setStatusType(status);
                        return dto;
                    })
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            logger.error("Error fetching daily guild leaderboard: ", ex);
            return List.of();
        }
    }



    /**
     * Get member leaderboard for a specific guild
     * Aggregates stats across all time periods for the given status type
     */
    public List<MemberLeaderboardDTO> getMemberLeaderboard(String guildId, int limit, LeaderboardMemberStatusEnum status) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Tuple> cq = cb.createTupleQuery();

            Root<MemberStatusEntity> statsRoot = cq.from(MemberStatusEntity.class);
            Join<MemberStatusEntity, GuildMemberEntity> memberJoin = statsRoot.join("guildMember");

            cq.select(cb.tuple(
                    memberJoin.get("memberId").alias("memberId"),
                    memberJoin.get("memberNickname").alias("nickname"),
                    memberJoin.get("xp").alias("xp"),
                    cb.sum(statsRoot.get("correctGuesses")).alias("totalGuesses")
            ));

            cq.where(cb.and(
                    cb.equal(memberJoin.get("guild").get("guildId"), guildId),
                    cb.equal(statsRoot.get("statusType"), status)
            ));

            cq.groupBy(
                    memberJoin.get("memberId"),
                    memberJoin.get("memberNickname"),
                    memberJoin.get("xp")
            );

            cq.orderBy(
                    cb.desc(cb.sum(statsRoot.get("correctGuesses"))),
                    cb.asc(memberJoin.get("memberId"))
            );

            return session.createQuery(cq)
                    .setMaxResults(limit)
                    .getResultList().stream()
                    .map(tuple -> {
                        MemberLeaderboardDTO dto = new MemberLeaderboardDTO();
                        dto.setMemberId(tuple.get("memberId", String.class));
                        dto.setNickname(tuple.get("nickname", String.class));
                        Number sum = tuple.get("totalGuesses", Number.class);
                        dto.setTotalCorrectGuesses(sum != null ? sum.intValue() : 0);
                        return dto;
                    })
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            logger.error("Error fetching member leaderboard: ", ex);
            return List.of();
        }
    }

    /**
     * Get daily member leaderboard for a specific guild
     * Only includes stats from today
     */
    public List<MemberLeaderboardDTO> getDailyMemberLeaderboard(String guildId, int limit, LeaderboardMemberStatusEnum status) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Tuple> cq = cb.createTupleQuery();

            Root<MemberStatusEntity> statsRoot = cq.from(MemberStatusEntity.class);
            Join<MemberStatusEntity, GuildMemberEntity> memberJoin = statsRoot.join("guildMember");

            LocalDate today = LocalDate.now();

            cq.select(cb.tuple(
                    memberJoin.get("memberId").alias("memberId"),
                    memberJoin.get("memberNickname").alias("nickname"),
                    memberJoin.get("xp").alias("xp"),
                    cb.sum(statsRoot.get("correctGuesses")).alias("totalGuesses")
            ));

            cq.where(cb.and(
                    cb.equal(memberJoin.get("guild").get("guildId"), guildId),
                    cb.equal(statsRoot.get("statusType"), status),
                    cb.equal(statsRoot.get("guessedAt"), today)
            ));

            cq.groupBy(
                    memberJoin.get("memberId"),
                    memberJoin.get("memberNickname"),
                    memberJoin.get("xp")
            );

            cq.orderBy(
                    cb.desc(cb.sum(statsRoot.get("correctGuesses"))),
                    cb.asc(memberJoin.get("memberId"))
            );

            return session.createQuery(cq)
                    .setMaxResults(limit)
                    .getResultList().stream()
                    .map(tuple -> {
                        MemberLeaderboardDTO dto = new MemberLeaderboardDTO();
                        dto.setMemberId(tuple.get("memberId", String.class));
                        dto.setNickname(tuple.get("nickname", String.class));
                        Number sum = tuple.get("totalGuesses", Number.class);
                        dto.setTotalCorrectGuesses(sum != null ? sum.intValue() : 0);
                        return dto;
                    })
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            logger.error("Error fetching daily member leaderboard: ", ex);
            return List.of();
        }
    }
}