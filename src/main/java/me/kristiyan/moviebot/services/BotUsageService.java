package me.kristiyan.moviebot.services;

import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.database.entities.activity.BotUsageEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotUsageService {
        private static final Logger logger = LoggerFactory.getLogger(BotUsageService.class);
        private final DatabaseManager databaseManager;

        public BotUsageService(DatabaseManager databaseManager) {
            this.databaseManager = databaseManager;
        }

        public boolean addActivity(String guildName, String guildId,
                                String memberId, String memberNickname, String command) {
                try (Session session = databaseManager.getSessionFactory().openSession()) {
                    Transaction tx = session.beginTransaction();
                    BotUsageEntity botUsageEntity = new BotUsageEntity();
                    botUsageEntity.setGuildId(guildId);
                    botUsageEntity.setMemberId(memberId);
                    botUsageEntity.setMemberNickname(memberNickname);
                    botUsageEntity.setCommand(command);
                    botUsageEntity.setGuildName(guildName);
                    session.persist(botUsageEntity);
                    tx.commit();
                    return true;

                } catch (Exception ex) {
                    logger.error(ex.fillInStackTrace().toString());
                    System.err.println(ex.getMessage());
                    return false;
                }
        }
}
