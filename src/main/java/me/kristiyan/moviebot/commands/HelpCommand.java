package me.kristiyan.moviebot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class HelpCommand {
    public static void handleHelpCommand(SlashCommandInteractionEvent event) {
        event.replyEmbeds(new EmbedBuilder()
                .setTitle("🎬 Guess The Movie: Help")
                .setDescription("Test your cinema knowledge! I'll show you a scene, and you guess the movie or anime title.")
                .setColor(new Color(245, 197, 24))
                .setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .addField("🎮 How to Play",
                        "1. **Start:** Use `/start` to begin a match.\n" +
                                "2. **Watch:** Scene will appear in the channel.\n" +
                                "3. **Solve:** Use `/guess <title>` to give your answer!",
                        false)
                .addField("👥 Social vs. 👤 Solo",
                        "• **Community Mode:** By default, **anyone** in the server can join the fun and guess!\n" +
                                "• **Solo Mode:** Want to test your own skills? Use the `solo` option in `/start` to lock the game to yourself.",
                        false)

                .addField("🔥 High Stakes",
                        "Too easy? Enable `high_stakes` in your `/start` command.",
                        false)

                .addField("🤖 Commands",
                        "`/start` • Start a new game where you have to guess the movie from a scene (with optional filters)\n" +
                                "`/start-emoji` • Start a game where you have to guess the movie from emojis (with optional filters). Premium only!\n" +
                                "`/start-quote • Start a game where you have to guess the movie from famous quotes (with optional filters). Premium only!\n" +
                                "`/start-zoomed` • Start a new game where you have to guess the movie from a zoomed in scene (with optional filters). Premium only!\n" +
                                "`/daily-challange` • Try to guess today's movie by clues. Premium only!\n" +
                                "`/hint` • Shows you useful hints to help your thinking. Unlimited uses only by premium!\n" +
                                "`/guess` • Submit your movie or anime title\n" +
                                "`/skip` • Give up and see the answer\n" +
                                "`/leaderboard` • Compare your rank globally and locally\n" +
                                "`/leaderboard-daily` • Check your rank for today\n" +
                                "`/updates` • See what's new in the latest version\n" +
                                "`/report [reason]` • Report an inaccurate movie for your current game\n" +
                                "`/ping` • Displays the latency of the bot\n" +
                                "`/profile (user)` • Displays your profile in the bot\n",
                        false)
                .addField("Notes:",
                        "In case the bot doesn't respond after using the command. Please check `/ping` or make sure the bot has **View Channel** permissions",
                        false)
                .setFooter("\uD83C\uDF7F Popcorn ready? Good luck!").build()).queue();

        // TODO add to profile: Accuracy in %. Win streaks.
        // TODO premium users get to choose their categories, while free users get random scenes
    }
}
