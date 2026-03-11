package me.kristiyan.moviebot.services;

import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.database.entities.guild.GuildEntity;
import me.kristiyan.moviebot.database.entities.guild.MemberEntity;
import net.dv8tion.jda.api.entities.Entitlement;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

public class PremiumService {
    private static final Logger logger = LoggerFactory.getLogger(PremiumService.class);
    private final DatabaseManager databaseManager;

    public PremiumService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Checks if a specific guild has an active premium subscription.
     */
    public boolean isGuildPremium(Session session, String guildId) {
        GuildEntity guild = session.find(GuildEntity.class, guildId);
        return guild != null && guild.isPremium();
    }

    /**
     * Checks if a specific member has an active global premium subscription.
     */
    public boolean isMemberPremium(Session session, String memberId) {
        MemberEntity member = session.find(MemberEntity.class, memberId);
        return member != null && member.isPremium();
    }

    /**
     * Checks if a member has premium access either via their own subscription
     * or through their guild's subscription.
     */
    public boolean isPremium(String memberId, String guildId) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            return isMemberPremium(session, memberId) || isGuildPremium(session, guildId);
        } catch (Exception ex) {
            logger.error(ex.fillInStackTrace().toString());
            System.err.println(ex.getMessage());
            return false;
        }
    }

    /**
     * Called on EntitlementCreateEvent — marks the member as premium from today
     * until the entitlement's end date (or 1 month fallback if Discord doesn't provide one).
     */
    public void onEntitlementCreate(Entitlement entitlement) {
        String memberId = String.valueOf(entitlement.getUserIdLong());
        LocalDate now = LocalDate.now();
        LocalDate expiresAt = entitlement.getTimeEnding() != null
                ? entitlement.getTimeEnding().toLocalDate()
                : now.plusMonths(1);
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                MemberEntity member = session.find(MemberEntity.class, memberId);
                if (member == null) {
                    logger.warn("Entitlement created for unknown member: {}", memberId);
                    return;
                }

                member.setSubscribedAt(now);
                member.setExpiresAt(expiresAt);
                session.merge(member);
                tx.commit();

                logger.info("Member {} subscribed. Expires: {}", memberId, expiresAt);
            } catch (Exception e) {
                tx.rollback();
                logger.error("Failed to process entitlement create for member {}", memberId, e);
                System.err.println(e.getMessage());
            }
        } catch (Exception ex) {
            logger.error(ex.fillInStackTrace().toString());
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Called on EntitlementUpdateEvent — if the entitlement has ended, revoke premium.
     * If it was renewed, refresh the expiry date.
     */
    public void onEntitlementUpdate(Entitlement entitlement) {
        String memberId = String.valueOf(entitlement.getUserIdLong());
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                MemberEntity member = session.find(MemberEntity.class, memberId);
                if (member == null) {
                    logger.warn("Entitlement updated for unknown member: {}", memberId);
                    return;
                }

                if (entitlement.isDeleted()) {
                    // Subscription expired — revoke premium
                    member.setExpiresAt(LocalDate.now());
                    logger.info("Member {} subscription ended.", memberId);
                } else if (entitlement.getTimeEnding() != null) {
                    // Subscription renewed — refresh expiry
                    member.setExpiresAt(entitlement.getTimeEnding().toLocalDate());
                    logger.info("Member {} subscription renewed. New expiry: {}", memberId, member.getExpiresAt());
                }

                session.merge(member);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                logger.error("Failed to process entitlement update for member {}", memberId, e);
                System.err.println(e.getMessage());
            }
        } catch (Exception ex) {
            logger.error(ex.fillInStackTrace().toString());
            System.err.println(ex.getMessage());
        }
    }
}