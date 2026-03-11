package me.kristiyan.moviebot.services;

import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.database.entities.guild.GuildEntity;
import me.kristiyan.moviebot.database.entities.guild.MemberEntity;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        // Assuming GuildEntity has an isPremium() method similar to MemberEntity
        return guild != null && guild.isPremium();
    }

    /**
     * Checks if a specific member has an active global premium subscription.
     */
    public boolean isMemberPremium(Session session, String memberId) {
        MemberEntity member = session.find(MemberEntity.class, memberId);
        return member != null && member.isPremium();
    }
}
