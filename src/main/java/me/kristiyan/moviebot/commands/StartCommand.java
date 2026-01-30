package me.kristiyan.moviebot.commands;

import me.kristiyan.moviebot.db.DatabaseManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class StartCommand {
    public static void handleStartCommand(SlashCommandInteractionEvent event, String guildId) {
        // Check if there's already an active movie
        if (DatabaseManager.getActiveMovie(guildId) != null) {
            event.reply("⚠️ There's already an active movie to guess! Use `/guess <title>` to make your guess (one attempt only!), or `/skip` to reveal the answer and start a new round.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get a random movie
        DatabaseManager.Movie movie = DatabaseManager.getRandomMovie();
        if (movie == null) {
            event.reply("❌ Could not retrieve movie information. Please contact support or try again.").setEphemeral(true).queue();
            return;
        }
        // Store it in the database
        DatabaseManager.setActiveMovie(guildId, movie.id);

        // Send the GIF with a prompt
        event.replyEmbeds(new EmbedBuilder()
                .setTitle("🎬 Movie Guessing Game!")
                .setDescription("Can you guess this movie? Use `/guess <title>` to make your guess!\n\n⚠️ **You only get ONE attempt!**")
                .setColor(Color.BLUE)
                .setImage(movie.gifUrl)
                .setFooter("Good luck! 🍿").build()).queue();
    }
}
