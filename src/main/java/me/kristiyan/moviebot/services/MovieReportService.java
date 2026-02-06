package me.kristiyan.moviebot.services;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.database.entities.movie.MovieEntity;
import me.kristiyan.moviebot.database.entities.reports.MovieReportEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.LocalDateTime;

public class MovieReportService {
    private static final Logger logger = LoggerFactory.getLogger(MovieReportService.class);
    private final DatabaseManager databaseManager;
    private TextChannel textChannel = null;

    public MovieReportService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void setTextChannel(JDA jda, String guildId, String channelId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            logger.error("Could not find report guild");
            System.err.println("Could not find report guild");
            return;
        }
        this.textChannel = guild.getTextChannelById(channelId);
        if  (textChannel == null) {
            logger.error("Could not find report text channel");
            System.err.println("Could not find report text channel");
        }
    }

    public boolean hasAlreadyReported(String memberId, MovieEntity movie) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<MovieReportEntity> root = cq.from(MovieReportEntity.class);

            cq.select(cb.count(root)).where(
                    cb.equal(root.get("memberId"), memberId),
                    cb.equal(root.get("movie"), movie)
            );

            return session.createQuery(cq).getSingleResult() > 0;
        } catch (Exception ex) {
            logger.error(ex.fillInStackTrace().toString());
            System.err.println(ex.getMessage());
            return false;
        }
    }

    public void submitReport(JDA jda, String memberId, String memberNickname, MovieEntity movie, String reason) {
        try (Session session = databaseManager.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                MovieReportEntity report = new MovieReportEntity();
                report.setMemberId(memberId);
                report.setMovie(movie);
                report.setReason(reason);
                session.persist(report);
                tx.commit();

                sentReport(jda, memberNickname, movie, reason);
            } catch (Exception ex) {
                if (tx != null) tx.rollback();
                logger.error(ex.fillInStackTrace().toString());
                System.err.println(ex.getMessage());
            }
        }
    }

    public void sentReport(JDA jda, String memberNickname, MovieEntity movie, String reason) {
        if (textChannel == null) {
            return;
        }
        textChannel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED)
                .setAuthor("Movie Report", null, jda.getSelfUser().getAvatarUrl())
                .addField("Title", movie.getTitle(), false)
                .addField("Gif", movie.getGifUrl(), false)
                .addField("Type", movie.getContentType(), false)
                .addField("Member", memberNickname, false)
                .addField("Reason", reason, false)
                .addField("Report At", LocalDateTime.now().toString(), false)
                .build()).queue();
    }
}
