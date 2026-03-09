package me.kristiyan.moviebot.services;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.database.dto.GuildLeaderboardDTO;
import me.kristiyan.moviebot.database.dto.MemberLeaderboardDTO;
import me.kristiyan.moviebot.database.entities.games.ActiveGameEntity;
import me.kristiyan.moviebot.database.entities.games.ActiveGuessEntity;
import me.kristiyan.moviebot.database.entities.leaderboards.GuildEntity;
import me.kristiyan.moviebot.database.entities.leaderboards.GuildMemberEntity;
import me.kristiyan.moviebot.database.entities.leaderboards.MemberStatusEntity;
import me.kristiyan.moviebot.enums.LeaderboardMemberStatus;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
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

            Transaction tx = session.beginTransaction();
            try {
                // 1. AGGREGATE scores first: Map<MemberId, PointsWon>
                Map<String, Long> pointsPerMember = correctGuesses.stream()
                        .collect(Collectors.groupingBy(ActiveGuessEntity::getMemberId, Collectors.counting()));

                Map<String, String> idToNickname = correctGuesses.stream()
                        .collect(Collectors.toMap(
                                ActiveGuessEntity::getMemberId,
                                ActiveGuessEntity::getMemberNickname,
                                (existing, replacement) -> existing
                        ));
                // 2. Get or create Guild entity
                GuildEntity guild = session.find(GuildEntity.class, guildId);
                if (guild == null) {
                    guild = new GuildEntity();
                    guild.setGuildId(guildId);
                    guild.setGuildName(guildName);
                    session.persist(guild);
                } else if (!guild.getGuildName().equals(guildName)) {
                    // Update guild name if it changed
                    guild.setGuildName(guildName);
                    session.merge(guild);
                }

                List<String> memberIds = new ArrayList<>(pointsPerMember.keySet());

                // 3. Fetch existing GuildMember entities
                CriteriaBuilder cb = session.getCriteriaBuilder();
                CriteriaQuery<GuildMemberEntity> memberQuery = cb.createQuery(GuildMemberEntity.class);
                Root<GuildMemberEntity> memberRoot = memberQuery.from(GuildMemberEntity.class);

                memberQuery.select(memberRoot).where(
                        cb.equal(memberRoot.get("guild").get("guildId"), guildId),
                        memberRoot.get("memberId").in(memberIds)
                );

                Map<String, GuildMemberEntity> existingMembers = session.createQuery(memberQuery)
                        .getResultStream()
                        .collect(Collectors.toMap(GuildMemberEntity::getMemberId, m -> m));

                // 4. Create or update GuildMember entities
                for (Map.Entry<String, Long> entry : pointsPerMember.entrySet()) {
                    String memberId = entry.getKey();
                    int pointsToAdd = entry.getValue().intValue();

                    GuildMemberEntity member = existingMembers.get(memberId);

                    if (member == null) {
                        // Create new member
                        member = new GuildMemberEntity();
                        member.setGuild(guild);
                        member.setMemberId(memberId);
                        member.setMemberNickname(idToNickname.getOrDefault(memberId, "Unknown User"));
                        member.setXp(pointsToAdd);
                        session.persist(member);
                        existingMembers.put(memberId, member);
                    } else {
                        // Update existing member
                        String newNickname = idToNickname.getOrDefault(memberId, member.getMemberNickname());
                        if (!member.getMemberNickname().equals(newNickname)) {
                            member.setMemberNickname(newNickname);
                        }
                        // Add XP
                        member.setXp(member.getXp() + pointsToAdd);
                        session.merge(member);
                    }
                }

                // Flush to ensure GuildMember IDs are generated
                session.flush();
                // 5. Fetch existing stats for today
                LocalDate today = LocalDate.now();
                CriteriaQuery<MemberStatusEntity> statsQuery = cb.createQuery(MemberStatusEntity.class);
                Root<MemberStatusEntity> statsRoot = statsQuery.from(MemberStatusEntity.class);
                // Get IDs of guild members
                List<Long> guildMemberIds = existingMembers.values().stream()
                        .map(GuildMemberEntity::getId)
                        .collect(Collectors.toList());

                statsQuery.select(statsRoot).where(
                        statsRoot.get("guildMember").get("id").in(guildMemberIds),
                        cb.equal(statsRoot.get("statusType"), leaderboardMemberStatus),
                        cb.equal(statsRoot.get("guessedAt"), today)
                );

                Map<Long, MemberStatusEntity> existingStats = session.createQuery(statsQuery)
                        .getResultStream()
                        .collect(Collectors.toMap(s -> s.getGuildMember().getId(), s -> s));

                // 6. Update or create stats
                for (Map.Entry<String, Long> entry : pointsPerMember.entrySet()) {
                    String memberId = entry.getKey();
                    int pointsToAdd = entry.getValue().intValue();

                    GuildMemberEntity guildMember = existingMembers.get(memberId);
                    if (guildMember == null) continue; // Should not happen
                    MemberStatusEntity stats = existingStats.get(guildMember.getId());
                    if (stats == null) {
                        // Create new stats record
                        stats = new MemberStatusEntity();
                        stats.setGuildMember(guildMember);
                        stats.setStatusType(leaderboardMemberStatus);
                        stats.setGuessedAt(today);
                        stats.setCorrectGuesses(pointsToAdd);
                        session.persist(stats);
                    } else {
                        // Update existing stats
                        stats.setCorrectGuesses(stats.getCorrectGuesses() + pointsToAdd);
                        session.merge(stats);
                    }
                }

                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        } catch (Exception ex) {
            logger.error(ex.fillInStackTrace().toString());
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Calculate level based on XP
     * Level 1 = 3 XP, Level 2 = 6 XP, Level 3 = 12 XP, Level 4 = 24 XP, etc.
     */
    public int calculateLevel(long xp) {
        if (xp < 3) return 0;

        int level = 1;
        int requiredXP = 3;
        int totalXP = 0;

        while (totalXP + requiredXP <= xp) {
            totalXP += requiredXP;
            level++;
            requiredXP *= 2; // Double the requirement for next level
        }

        return level;
    }

    /**
     * Get guild leaderboard by aggregating stats across all members in each guild
     */
    public List<GuildLeaderboardDTO> getGuildLeaderboard(LeaderboardMemberStatus leaderboardMemberStatus, int limit) {
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

            cq.where(cb.equal(statsRoot.get("statusType"), leaderboardMemberStatus));

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
                        dto.setStatusType(leaderboardMemberStatus);
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
    public List<GuildLeaderboardDTO> getDailyGuildLeaderboard(LeaderboardMemberStatus status, int limit) {
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
    public List<MemberLeaderboardDTO> getMemberLeaderboard(String guildId, int limit, LeaderboardMemberStatus status) {
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
                        dto.setXp(tuple.get("xp", Long.class));
                        Number sum = tuple.get("totalGuesses", Number.class);
                        dto.setTotalCorrectGuesses(sum != null ? sum.intValue() : 0);
                        dto.setLevel(calculateLevel(dto.getXp()));
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
    public List<MemberLeaderboardDTO> getDailyMemberLeaderboard(String guildId, int limit, LeaderboardMemberStatus status) {
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
                        dto.setXp(tuple.get("xp", Long.class));
                        Number sum = tuple.get("totalGuesses", Number.class);
                        dto.setTotalCorrectGuesses(sum != null ? sum.intValue() : 0);
                        dto.setLevel(calculateLevel(dto.getXp()));
                        return dto;
                    })
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            logger.error("Error fetching daily member leaderboard: ", ex);
            return List.of();
        }
    }
}