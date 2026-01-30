package me.kristiyan.moviebot.commands;

import me.kristiyan.moviebot.db.DatabaseManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.List;

public class LeaderboardCommand {
    public static void handleLeaderboardCommand(SlashCommandInteractionEvent event, String guildId) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🏆 Leaderboards")
                .setColor(Color.ORANGE);

        // Get guild leaderboard
        List<DatabaseManager.LeaderboardEntry> guildLeaderboard = DatabaseManager.getGuildLeaderboard(10);

        if (!guildLeaderboard.isEmpty()) {
            StringBuilder guildBoard = new StringBuilder();
            for (int i = 0; i < guildLeaderboard.size(); i++) {
                DatabaseManager.LeaderboardEntry entry = guildLeaderboard.get(i);
                String medal = i == 0 ? "🥇" : i == 1 ? "🥈" : i == 2 ? "🥉" : (i + 1) + ".";
                guildBoard.append(medal)
                        .append(" **")
                        .append(entry.name)
                        .append("** - ")
                        .append(entry.score)
                        .append(" correct guess")
                        .append(entry.score == 1 ? "" : "es")
                        .append("\n");
            }
            embed.addField("🌍 Top Servers", guildBoard.toString(), false);
        } else {
            embed.addField("🌍 Top Servers", "No data yet!", false);
        }

        // Get member leaderboard for this guild
        List<DatabaseManager.LeaderboardEntry> memberLeaderboard = DatabaseManager.getMemberLeaderboard(guildId, 10);

        if (!memberLeaderboard.isEmpty()) {
            StringBuilder memberBoard = new StringBuilder();
            for (int i = 0; i < memberLeaderboard.size(); i++) {
                DatabaseManager.LeaderboardEntry entry = memberLeaderboard.get(i);
                memberBoard.append(i == 0 ? "🥇" : i == 1 ? "🥈" : i == 2 ? "🥉" : (i + 1) + ".")
                        .append(" **")
                        .append(entry.name)
                        .append("** - ")
                        .append(entry.score)
                        .append(" correct guess")
                        .append(entry.score == 1 ? "" : "es")
                        .append("\n");
            }
            embed.addField("👥 Top Players (This Server)", memberBoard.toString(), false);
        } else {
            embed.addField("👥 Top Players (This Server)", "No data yet!", false);
        }

        embed.setFooter("Keep playing to climb the leaderboard! 🎬");

        event.replyEmbeds(embed.build()).queue();
    }
}
