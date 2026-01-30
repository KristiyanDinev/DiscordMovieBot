package me.kristiyan.moviebot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class HelpCommand {
    public static void handleHelpCommand(SlashCommandInteractionEvent event) {
        event.replyEmbeds(new EmbedBuilder()
                .setTitle("🎬 Guess The Movie: Help")
                .setDescription("Test your cinema knowledge! I'll show you a GIF, and you guess the movie or anime title.")
                .setColor(new Color(245, 197, 24))
                .addField("🎮 How to Play",
                        "1. Type `/start` to spawn a random movie GIF.\n" +
                                "2. Recognize the scene?\n" +
                                "3. Use `/guess <title>` to lock in your answer!",
                        false)

                .addField("⚠️ The Golden Rule",
                        "You only get **ONE** guess per movie!\n" +
                                "If you guess wrong, the round ends and the answer is revealed. No second chances!",
                        false)
                .addField("Commands",
                        "`/start` • Start a new game round\n" +
                                "`/guess [title]` • Submit your answer\n" +
                                "`/skip` • Skip the guess and see the answer\n" +
                                "`/leaderboard` • View server and global stats",
                        false)
                .setFooter("Good luck! 🍿").build()).queue();
    }
}
