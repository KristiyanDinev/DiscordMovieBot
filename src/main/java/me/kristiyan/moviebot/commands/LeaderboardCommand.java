package me.kristiyan.moviebot.commands;

import me.kristiyan.moviebot.database.entities.leaderboards.MemberStatusEntity;
import me.kristiyan.moviebot.enums.LeaderboardMemberStatus;
import me.kristiyan.moviebot.enums.LeaderboardTypeEnum;
import me.kristiyan.moviebot.services.ServiceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.List;
import java.util.function.Function;

public class LeaderboardCommand {

    public static void handleLeaderboardCommand(SlashCommandInteractionEvent event,
                                                String guildId, ServiceManager serviceManager,
                                                LeaderboardTypeEnum leaderboardTypeEnum) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🏆 Leaderboards")
                .setColor(Color.ORANGE)
                .setFooter("Keep playing to climb the leaderboard! 🎬");

        var lbService = serviceManager.getLeaderboardsService();

        List<MemberStatusEntity> GuildTopGuessesHighStakes;
        List<MemberStatusEntity> GuildTopGuesses;

        List<MemberStatusEntity> MembersTopGuessesHighStakes;
        List<MemberStatusEntity> MembersTopGuesses;

        switch (leaderboardTypeEnum) {
            case GLOBAL: {
                GuildTopGuessesHighStakes =
                        lbService.getGuildLeaderboard(LeaderboardMemberStatus.TOP_GUESSES_HIGH_STAKES, 10);
                GuildTopGuesses = lbService.getGuildLeaderboard(LeaderboardMemberStatus.TOP_GUESSES, 10);

                MembersTopGuessesHighStakes =
                        lbService.getMemberLeaderboard(guildId, 10, LeaderboardMemberStatus.TOP_GUESSES_HIGH_STAKES);
                MembersTopGuesses =
                        lbService.getMemberLeaderboard(guildId, 10, LeaderboardMemberStatus.TOP_GUESSES);
                break;
            }
            case DAILY: {
                GuildTopGuessesHighStakes =
                        lbService.getDailyGuildLeaderboard(LeaderboardMemberStatus.TOP_GUESSES_HIGH_STAKES, 10);
                GuildTopGuesses = lbService.getDailyGuildLeaderboard(LeaderboardMemberStatus.TOP_GUESSES, 10);

                MembersTopGuessesHighStakes =
                        lbService.getDailyMemberLeaderboard(guildId, 10, LeaderboardMemberStatus.TOP_GUESSES_HIGH_STAKES);
                MembersTopGuesses =
                        lbService.getDailyMemberLeaderboard(guildId, 10, LeaderboardMemberStatus.TOP_GUESSES);
            }
            default: {
                return;
            }
        }

        // 1. Global Server Leaderboards
        addLeaderboardField(embed, "🌍 Top Servers (High Stakes 🔥)",
                GuildTopGuessesHighStakes, MemberStatusEntity::getGuildName);

        addLeaderboardField(embed, "🌍 Top Servers",
                GuildTopGuesses, MemberStatusEntity::getGuildName);

        // 2. Local Member Leaderboards
        addLeaderboardField(embed, "👥 Top Players (This Server) (High Stakes 🔥)",
                MembersTopGuessesHighStakes, MemberStatusEntity::getMemberNickname);

        addLeaderboardField(embed, "👥 Top Players (This Server)",
                MembersTopGuesses, MemberStatusEntity::getMemberNickname);

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