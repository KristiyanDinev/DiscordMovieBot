package me.kristiyan.moviebot.commands;

import me.kristiyan.moviebot.database.entities.leaderboards.MemberStatusEntity;
import me.kristiyan.moviebot.enums.LeaderboardMemberStatus;
import me.kristiyan.moviebot.services.ServiceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.List;
import java.util.function.Function;

public class LeaderboardCommand {

    public static void handleLeaderboardCommand(SlashCommandInteractionEvent event,
                                                String guildId, ServiceManager serviceManager) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🏆 Leaderboards")
                .setColor(Color.ORANGE)
                .setFooter("Keep playing to climb the leaderboard! 🎬");

        var lbService = serviceManager.getLeaderboardsService();

        // 1. Global Server Leaderboards
        addLeaderboardField(embed, "🌍 Top Servers (High Stakes 🔥)",
                lbService.getGuildLeaderboard(LeaderboardMemberStatus.TOP_GUESSES_HIGH_STAKES, 10),
                MemberStatusEntity::getGuildName);

        addLeaderboardField(embed, "🌍 Top Servers",
                lbService.getGuildLeaderboard(LeaderboardMemberStatus.TOP_GUESSES, 10),
                MemberStatusEntity::getGuildName);

        // 2. Local Member Leaderboards
        addLeaderboardField(embed, "👥 Top Players (This Server) (High Stakes 🔥)",
                lbService.getMemberLeaderboard(guildId, 10, LeaderboardMemberStatus.TOP_GUESSES_HIGH_STAKES),
                MemberStatusEntity::getMemberNickname);

        addLeaderboardField(embed, "👥 Top Players (This Server)",
                lbService.getMemberLeaderboard(guildId, 10, LeaderboardMemberStatus.TOP_GUESSES),
                MemberStatusEntity::getMemberNickname);

        event.replyEmbeds(embed.build()).queue();
    }

    /**
     * Helper method to format lists into embed fields to avoid code duplication.
     */
    private static void addLeaderboardField(EmbedBuilder embed, String title,
                                            List<MemberStatusEntity> entries,
                                            Function<MemberStatusEntity, String> nameExtractor) {
        if (entries == null || entries.isEmpty()) {
            embed.addField(title, "*No data yet!*", false);
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            MemberStatusEntity entry = entries.get(i);
            int score = entry.getCorrectGuesses();
            String name = nameExtractor.apply(entry);

            // Determine Medal/Rank
            String rank = switch (i) {
                case 0 -> "🥇";
                case 1 -> "🥈";
                case 2 -> "🥉";
                default -> (i + 1) + ".";
            };

            builder.append(rank).append(" **").append(name).append("** - ")
                    .append(score).append(" correct guess")
                    .append(score == 1 ? "" : "es")
                    .append("\n");
        }

        embed.addField(title, builder.toString(), false);
    }
}