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
import me.kristiyan.moviebot.enums.LeaderboardMemberStatus;
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

    public void recordCorrectGuesses(String guildId, String guildName, LeaderboardMemberStatus leaderboardMemberStatus) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            ActiveGameEntity activeGameEntry = serviceManager.getActiveGameService().getActiveGameEntity(guildId);
            if (activeGameEntry == null) return;

            List<ActiveGuessEntity> correctGuesses = serviceManager.getActiveGameService().getActiveGuessEntities(activeGameEntry);
            if (correctGuesses.isEmpty()) return;

            Transaction tx = session.beginTransaction();
            try {
                // 1. Aggregate scores and collect nicknames per member
                Map<String, Long> pointsPerMember = correctGuesses.stream()
                        .collect(Collectors.groupingBy(ActiveGuessEntity::getMemberId, Collectors.counting()));

                Map<String, String> idToNickname = correctGuesses.stream()
                        .collect(Collectors.toMap(
                                ActiveGuessEntity::getMemberId,
                                ActiveGuessEntity::getMemberNickname,
                                (existing, replacement) -> existing
                        ));

                // 2. Get or create GuildEntity
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

                List<String> memberIds = new ArrayList<>(pointsPerMember.keySet());

                // 3. Resolve or create MemberEntity for each guesser
                Map<String, MemberEntity> memberEntities = new HashMap<>();
                for (String memberId : memberIds) {
                    MemberEntity memberEntity = session.find(MemberEntity.class, memberId);
                    if (memberEntity == null) {
                        memberEntity = new MemberEntity();
                        memberEntity.setMemberId(memberId);
                        memberEntity.setMemberName(idToNickname.getOrDefault(memberId, "Unknown User"));
                        session.persist(memberEntity);
                    } else {
                        // Sync display name if it changed
                        String latestNickname = idToNickname.getOrDefault(memberId, memberEntity.getMemberName());
                        if (!memberEntity.getMemberName().equals(latestNickname)) {
                            memberEntity.setMemberName(latestNickname);
                            session.merge(memberEntity);
                        }
                    }
                    memberEntities.put(memberId, memberEntity);
                }

                // 4. Fetch existing GuildMemberEntities — join through member relation
                CriteriaBuilder cb = session.getCriteriaBuilder();
                CriteriaQuery<GuildMemberEntity> memberQuery = cb.createQuery(GuildMemberEntity.class);
                Root<GuildMemberEntity> memberRoot = memberQuery.from(GuildMemberEntity.class);

                memberQuery.select(memberRoot).where(
                        cb.equal(memberRoot.get("guild").get("guildId"), guildId),
                        memberRoot.get("member").get("memberId").in(memberIds)  // fixed: was "memberId" raw field
                );

                Map<String, GuildMemberEntity> existingMembers = session.createQuery(memberQuery)
                        .getResultStream()
                        .collect(Collectors.toMap(m -> m.getMember().getMemberId(), m -> m));

                // 5. Create or update GuildMemberEntities
                for (Map.Entry<String, Long> entry : pointsPerMember.entrySet()) {
                    String memberId = entry.getKey();
                    int pointsToAdd = entry.getValue().intValue();

                    GuildMemberEntity guildMember = existingMembers.get(memberId);
                    if (guildMember == null) {
                        guildMember = new GuildMemberEntity();
                        guildMember.setGuild(guild);
                        guildMember.setMember(memberEntities.get(memberId));  // fixed: was setMemberId()
                        guildMember.setMemberNickname(idToNickname.getOrDefault(memberId, "Unknown User"));
                        guildMember.setXp(pointsToAdd);
                        session.persist(guildMember);
                        existingMembers.put(memberId, guildMember);
                    } else {
                        String newNickname = idToNickname.getOrDefault(memberId, guildMember.getMemberNickname());
                        if (!guildMember.getMemberNickname().equals(newNickname)) {
                            guildMember.setMemberNickname(newNickname);
                        }
                        guildMember.setXp(guildMember.getXp() + pointsToAdd);
                        session.merge(guildMember);
                    }
                }

                // Flush to ensure GuildMember IDs are generated before stats step
                session.flush();

                // 6. Fetch existing MemberStatusEntity records for today
                LocalDate today = LocalDate.now();
                List<Long> guildMemberIds = existingMembers.values().stream()
                        .map(GuildMemberEntity::getId)
                        .collect(Collectors.toList());

                CriteriaQuery<MemberStatusEntity> statsQuery = cb.createQuery(MemberStatusEntity.class);
                Root<MemberStatusEntity> statsRoot = statsQuery.from(MemberStatusEntity.class);

                statsQuery.select(statsRoot).where(
                        statsRoot.get("guildMember").get("id").in(guildMemberIds),
                        cb.equal(statsRoot.get("statusType"), leaderboardMemberStatus),
                        cb.equal(statsRoot.get("guessedAt"), today)
                );

                Map<Long, MemberStatusEntity> existingStats = session.createQuery(statsQuery)
                        .getResultStream()
                        .collect(Collectors.toMap(s -> s.getGuildMember().getId(), s -> s));

                // 7. Create or update MemberStatusEntity records
                for (Map.Entry<String, Long> entry : pointsPerMember.entrySet()) {
                    String memberId = entry.getKey();
                    int pointsToAdd = entry.getValue().intValue();

                    GuildMemberEntity guildMember = existingMembers.get(memberId);
                    if (guildMember == null) continue;

                    MemberStatusEntity stats = existingStats.get(guildMember.getId());
                    if (stats == null) {
                        stats = new MemberStatusEntity();
                        stats.setGuildMember(guildMember);
                        stats.setStatusType(leaderboardMemberStatus);
                        stats.setGuessedAt(today);
                        stats.setCorrectGuesses(pointsToAdd);
                        session.persist(stats);
                    } else {
                        stats.setCorrectGuesses(stats.getCorrectGuesses() + pointsToAdd);
                        session.merge(stats);
                    }
                }

                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                logger.error("Failed to record correct guesses for guild [{}]", guildId, e);
                throw e;
            }
        } catch (Exception ex) {
            logger.error("Session error while recording correct guesses for guild [{}]", guildId, ex);
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


    public Optional<GuildMemberEntity> getGuildMember(String guildId, String memberId) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<GuildMemberEntity> query = cb.createQuery(GuildMemberEntity.class);
            Root<GuildMemberEntity> root = query.from(GuildMemberEntity.class);
            query.select(root)
                    .where(
                            cb.equal(root.get("guild").get("guildId"), guildId),
                            cb.equal(root.get("memberId"), memberId)
                    );
            return Optional.ofNullable(session.createQuery(query).getSingleResultOrNull());
        } catch (Exception e) {
            logger.error(e.fillInStackTrace().toString());
            System.err.println(e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<GuildEntity> getGuild(String guildId) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<GuildEntity> query = cb.createQuery(GuildEntity.class);
            Root<GuildEntity> root = query.from(GuildEntity.class);
            query.select(root).where(cb.equal(root.get("guildId"), guildId));
            return Optional.ofNullable(session.createQuery(query).getSingleResultOrNull());
        } catch (Exception e) {
            logger.error(e.fillInStackTrace().toString());
            System.err.println(e.getMessage());
            return Optional.empty();
        }
    }

    public boolean createGuild(String guildId, String guildName) {
        Transaction tx = null;
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            GuildEntity guildEntity = new GuildEntity();
            guildEntity.setGuildId(guildId);
            guildEntity.setGuildName(guildName);
            session.persist(guildEntity);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            logger.error(e.fillInStackTrace().toString());
            System.err.println(e.getMessage());
            return false;
        }
    }

    public Optional<GuildMemberEntity> createGuildMember(String guildId, String guildName, String memberId,
                                                         String memberName, long xp, LocalDate subscribedAt,
                                                         LocalDate expiresAt) {
        GuildEntity guildEntity = getGuild(guildId).orElseGet(() -> {
            if (createGuild(guildId, guildName)) {
                return getGuild(guildId).orElse(null);
            }
            return null;
        });

        if (guildEntity == null) {
            logger.error("Failed to resolve guild [{}] for member creation", guildId);
            return Optional.empty();
        }

        Transaction tx = null;
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Resolve or create the global MemberEntity
            MemberEntity memberEntity = session.find(MemberEntity.class, memberId);
            if (memberEntity == null) {
                memberEntity = new MemberEntity();
                memberEntity.setMemberId(memberId);
                memberEntity.setMemberName(memberName);
                memberEntity.setSubscribedAt(subscribedAt);
                memberEntity.setExpiresAt(expiresAt);
                session.persist(memberEntity);
            } else {
                // Keep name and premium dates up to date
                memberEntity.setMemberName(memberName);
                memberEntity.setSubscribedAt(subscribedAt);
                memberEntity.setExpiresAt(expiresAt);
                session.merge(memberEntity);
            }

            GuildMemberEntity guildMemberEntity = new GuildMemberEntity();
            guildMemberEntity.setGuild(guildEntity);
            guildMemberEntity.setMember(memberEntity);
            guildMemberEntity.setMemberNickname(memberName);
            guildMemberEntity.setXp(xp);
            session.persist(guildMemberEntity);

            tx.commit();
            return Optional.of(guildMemberEntity);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            logger.error("Failed to create guild member [{}, {}]", guildId, memberId, e);
            return Optional.empty();
        }
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