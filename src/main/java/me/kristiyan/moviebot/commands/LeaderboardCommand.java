package me.kristiyan.moviebot.commands;

import me.kristiyan.moviebot.database.dto.GuildLeaderboardDTO;
import me.kristiyan.moviebot.database.dto.MemberLeaderboardDTO;
import me.kristiyan.moviebot.enums.LeaderboardMemberStatusEnum;
import me.kristiyan.moviebot.enums.LeaderboardTypeEnum;
import me.kristiyan.moviebot.services.ServiceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.List;

public class LeaderboardCommand {

    public static void handleLeaderboardCommand(SlashCommandInteractionEvent event,
                                                String guildId, ServiceManager serviceManager,
                                                LeaderboardTypeEnum leaderboardTypeEnum) {
        String leaderboardTypeEnumName = leaderboardTypeEnum.toString();
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🏆 " + leaderboardTypeEnumName.charAt(0) +
                        leaderboardTypeEnumName.toLowerCase().substring(1) + " Leaderboards")
                .setColor(Color.ORANGE)
                .setFooter("Keep playing to climb the leaderboard! 🎬");

        var lbService = serviceManager.getLeaderboardsService();

        List<GuildLeaderboardDTO> guildTopGuessesHighStakes;
        List<GuildLeaderboardDTO> guildTopGuesses;

        List<MemberLeaderboardDTO> membersTopGuessesHighStakes;
        List<MemberLeaderboardDTO> membersTopGuesses;

        switch (leaderboardTypeEnum) {
            case GLOBAL: {
                guildTopGuessesHighStakes =
                        lbService.getGuildLeaderboard(LeaderboardMemberStatusEnum.TOP_GUESSES_HIGH_STAKES, 10);
                guildTopGuesses =
                        lbService.getGuildLeaderboard(LeaderboardMemberStatusEnum.TOP_GUESSES, 10);

                membersTopGuessesHighStakes =
                        lbService.getMemberLeaderboard(guildId, 10, LeaderboardMemberStatusEnum.TOP_GUESSES_HIGH_STAKES);
                membersTopGuesses =
                        lbService.getMemberLeaderboard(guildId, 10, LeaderboardMemberStatusEnum.TOP_GUESSES);
                break;
            }
            case DAILY: {
                guildTopGuessesHighStakes =
                        lbService.getDailyGuildLeaderboard(LeaderboardMemberStatusEnum.TOP_GUESSES_HIGH_STAKES, 10);
                guildTopGuesses =
                        lbService.getDailyGuildLeaderboard(LeaderboardMemberStatusEnum.TOP_GUESSES, 10);

                membersTopGuessesHighStakes =
                        lbService.getDailyMemberLeaderboard(guildId, 10, LeaderboardMemberStatusEnum.TOP_GUESSES_HIGH_STAKES);
                membersTopGuesses =
                        lbService.getDailyMemberLeaderboard(guildId, 10, LeaderboardMemberStatusEnum.TOP_GUESSES);
                break;
            }
            default: {
                event.reply("❌ Invalid leaderboard type!").setEphemeral(true).queue();
                return;
            }
        }

        // 1. Global Server Leaderboards
        addGuildLeaderboardField(embed, "🌍 Top Servers (High Stakes 🔥)",
                guildTopGuessesHighStakes);

        addGuildLeaderboardField(embed, "🌍 Top Servers",
                guildTopGuesses);

        // 2. Local Member Leaderboards
        addMemberLeaderboardField(embed, "👥 Top Players (This Server) (High Stakes 🔥)",
                membersTopGuessesHighStakes);

        addMemberLeaderboardField(embed, "👥 Top Players (This Server)",
                membersTopGuesses);

        event.replyEmbeds(embed.build()).queue();
    }

    /**
     * Helper method to format guild leaderboard into embed field
     */
    private static void addGuildLeaderboardField(EmbedBuilder embed, String title,
                                                 List<GuildLeaderboardDTO> entries) {
        if (entries == null || entries.isEmpty()) {
            embed.addField(title, "*No data yet!*", false);
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            GuildLeaderboardDTO entry = entries.get(i);
            int score = entry.getTotalCorrectGuesses();
            String name = entry.getGuildName();

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

    /**
     * Helper method to format member leaderboard into embed field
     */
    private static void addMemberLeaderboardField(EmbedBuilder embed, String title,
                                                  List<MemberLeaderboardDTO> entries) {
        if (entries == null || entries.isEmpty()) {
            embed.addField(title, "*No data yet!*", false);
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            MemberLeaderboardDTO entry = entries.get(i);
            int score = entry.getTotalCorrectGuesses();
            String name = entry.getNickname();

            // Determine Medal/Rank
            String rank = switch (i) {
                case 0 -> "🥇";
                case 1 -> "🥈";
                case 2 -> "🥉";
                default -> (i + 1) + ".";
            };

            builder.append(rank).append(" **").append(name).append("**").append(" - ")
                    .append(score).append(" correct guess")
                    .append(score == 1 ? "" : "es")
                    .append("\n");
        }

        embed.addField(title, builder.toString(), false);
    }
}