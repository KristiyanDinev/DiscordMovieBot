package me.kristiyan.moviebot.commands;

import me.kristiyan.moviebot.database.entities.guild.GuildMemberEntity;
import me.kristiyan.moviebot.services.LeaderboardsService;
import me.kristiyan.moviebot.services.ServiceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class ProfileCommand {

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color FREE_COLOR    = new Color(0x6DD5ED);   // cyan-blue
    private static final Color PREMIUM_COLOR = new Color(0xC9A84C);   // gold

    // XP needed per level (simple linear formula — swap for your real one)
    private static final long XP_PER_LEVEL = 1_000L;

    public static void handleProfileCommand(SlashCommandInteractionEvent event,
                                            Guild guild, Member member,
                                            ServiceManager serviceManager) {
        String guildId   = guild.getId();
        String guildName = guild.getName();
        String memberId  = member.getId();
        String memberName = member.getEffectiveName();

        LeaderboardsService leaderboardsService = serviceManager.getLeaderboardsService();

        Optional<GuildMemberEntity> guildMemberEntityOptional =
                leaderboardsService.getGuildMember(guildId, memberId);
        if (guildMemberEntityOptional.isEmpty()) {
            guildMemberEntityOptional =
                    leaderboardsService.createGuildMember(guildId, guildName, memberId, memberName, 0L, null, null);
        }

        if (guildMemberEntityOptional.isEmpty()) {
            event.reply("❌ Error fetching your profile. Please try again later.")
                    .setEphemeral(true).queue();
            return;
        }

        GuildMemberEntity entity = guildMemberEntityOptional.get();
        EmbedBuilder embed = entity.isPremium()
                ? buildPremiumEmbed(member, entity)
                : buildFreeEmbed(member, entity);

        event.replyEmbeds(embed.build()).queue();
    }

    // ── Free profile ─────────────────────────────────────────────────────────
    private static EmbedBuilder buildFreeEmbed(Member member, GuildMemberEntity entity) {
        long xp      = entity.getXp();
        int  level   = computeLevel(xp);
        long xpToNext = xpForNextLevel(level);
        String xpBar = buildXpBar(xp, xpToNext, 12, false);

        return new EmbedBuilder()
                .setColor(FREE_COLOR)
                .setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl())
                .setThumbnail(member.getEffectiveAvatarUrl())

                // XP / Level
                .addField("▸ Level",
                        "```" + level + "```",
                        true)
                .addField("▸ Experience",
                        xpBar + "\n`" + xp + " / " + xpToNext + " XP`",
                        true)

                .addBlankField(false)

                // Watchlist
                .addField("🎬  Watchlist",
                        formatList(entity.getWatchlist(), "Nothing queued yet — add some films!"),
                        false)

                // Fav genres
                .addField("🎭  Mostly Guessed Genres",
                        formatTags(entity.getFavouriteGenres()),
                        true)

                // Fav movies
                .addField("⭐  Favourite Movies",
                        formatList(entity.getFavouriteMovies(), "No favourites set yet."),
                        false)

                // Upgrade nudge
                .setFooter("✨ Upgrade to Premium for an enhanced experience  ·  /upgrade")
                .setTimestamp(java.time.Instant.now());
    }

    // ── Premium profile ───────────────────────────────────────────────────────
    private static EmbedBuilder buildPremiumEmbed(Member member, GuildMemberEntity entity) {
        long xp      = entity.getXp();
        int  level   = computeLevel(xp);
        long xpToNext = xpForNextLevel(level);
        String xpBar = buildXpBar(xp, xpToNext, 12, true);
        String rank  = getPremiumRank(level);

        return new EmbedBuilder()
                .setColor(PREMIUM_COLOR)
                .setAuthor("✦  " + member.getEffectiveName(), null, member.getEffectiveAvatarUrl())
                .setThumbnail(member.getEffectiveAvatarUrl())

                // Thank-you banner replaces a blank description
                .setDescription(
                        "**Thank you for your support ❤️**\n" +
                                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Level + rank side by side
                .addField("✦  Level",
                        "```fix\n" + level + "\n```",
                        true)
                .addField("✦  Rank",
                        "```fix\n" + rank + "\n```",
                        true)

                // XP bar full-width
                .addField("✦  Experience",
                        xpBar + "\n`" + String.format("%,d", xp) + " / " + String.format("%,d", xpToNext) + " XP`",
                        false)

                // Watchlist
                .addField("🎬  Watchlist",
                        formatList(entity.getWatchlist(), "*Nothing queued yet.*"),
                        false)

                // Fav genres
                .addField("🎭  Mostly Guessed Genres",
                        formatTags(entity.getFavouriteGenres()),
                        true)

                // Fav movies
                .addField("⭐  All-Time Favourites",
                        formatList(entity.getFavouriteMovies(), "*No favourites set yet.*"),
                        false)

                .setFooter("✦ Premium Member  ·  MovieBot", null)
                .setTimestamp(java.time.Instant.now());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Renders a filled/empty progress bar. */
    private static String buildXpBar(long xp, long xpToNext, int blocks, boolean premium) {
        int filled = (int) Math.round(((double) xp / xpToNext) * blocks);
        filled = Math.max(0, Math.min(filled, blocks));
        String fill  = premium ? "◆" : "■";
        String empty = premium ? "◇" : "□";
        return fill.repeat(filled) + empty.repeat(blocks - filled);
    }

    /** Formats a list of strings as a numbered markdown block. */
    private static String formatList(List<String> items, String fallback) {
        if (items == null || items.isEmpty()) return fallback;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append("`").append(String.format("%02d", i + 1)).append("` ")
                    .append(items.get(i));
            if (i < items.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    /** Formats genres as inline code tags. */
    private static String formatTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) return "*None set yet.*";
        StringBuilder sb = new StringBuilder();
        for (String tag : tags) sb.append("`").append(tag).append("` ");
        return sb.toString().trim();
    }

    private static int computeLevel(long xp) {
        return (int) Math.max(1, xp / XP_PER_LEVEL);
    }

    private static long xpForNextLevel(int level) {
        return (long) (level + 1) * XP_PER_LEVEL;
    }

    private static String getPremiumRank(int level) {
        if (level >= 50) return "Legendary";
        if (level >= 30) return "Elite";
        if (level >= 15) return "Veteran";
        if (level >= 5)  return "Rising Star";
        return "Newcomer";
    }
}