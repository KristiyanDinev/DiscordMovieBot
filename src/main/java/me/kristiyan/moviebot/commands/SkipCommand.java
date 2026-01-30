package me.kristiyan.moviebot.commands;

import me.kristiyan.moviebot.db.DatabaseManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class SkipCommand {
    public static void handleSkipCommand(SlashCommandInteractionEvent event, String guildId) {
        // Check if there's an active movie
        DatabaseManager.ActiveMovie activeMovie = DatabaseManager.getActiveMovie(guildId);

        if (activeMovie == null) {
            event.reply("⚠️ No active movie to skip! Use `/start` to begin a new round.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Clear the current movie and all guesses
        DatabaseManager.clearActiveMovie(guildId);

        // Get a new random movie
        DatabaseManager.Movie movie = DatabaseManager.getRandomMovie();
        if (movie == null) {
            event.reply("❌ Could not retrieve movie information. Please contact support or try again.").setEphemeral(true).queue();
            return;
        }

        // Store it in the database
        DatabaseManager.setActiveMovie(guildId, movie.id);

        // Send message revealing the previous answer and showing new movie
        event.replyEmbeds(new EmbedBuilder()
                .setTitle("⏭️ Movie Skipped!")
                .setDescription("The previous movie was: **" + activeMovie.title + "**\n\nHere's a new one! Can you guess it?\n\n⚠️ **Remember: Only ONE attempt!**")
                .setImage(movie.gifUrl)
                .setColor(Color.ORANGE)
                .setFooter("Use /guess <title> to make your guess! 🍿").build()).queue();
    }
}
