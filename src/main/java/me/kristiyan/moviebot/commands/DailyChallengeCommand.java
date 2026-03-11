package me.kristiyan.moviebot.commands;

import me.kristiyan.moviebot.database.entities.movie.MovieEntity;
import me.kristiyan.moviebot.database.entities.movie.MovieGenreEntity;
import me.kristiyan.moviebot.services.ServiceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.Optional;

public class DailyChallengeCommand {

    public static void handleDailyChallengeCommand(SlashCommandInteractionEvent event, ServiceManager serviceManager) {
        Optional<MovieEntity> movieEntityOptional = serviceManager.getDailyChallengeService().getTodayMovie();
        if (movieEntityOptional.isEmpty()) {
            event.reply("❌ No daily challenge yet!").setEphemeral(true).queue();
            return;
        }
        MovieEntity movie = movieEntityOptional.get();
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🎬 Daily Challenge — Can you guess today's movie?")
                .setDescription(
                        "**Hint:** " + movie.getHint() + "\n\n" +
                                "**Emoji clue:** " + movie.getEmoji() + "\n\n" +
                                "> *\"" + movie.getQuote() + "\"*"
                )
                .addField("📅 Year", String.valueOf(movie.getYear()), true)
                .addField("⏱ Duration", movie.getDuration() + " min", true)
                .addField("🎭 Genre", movie.getGenres().stream()
                        .map(MovieGenreEntity::getGenre)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("Unknown"), true)
                .setColor(new Color(255, 180, 0))
                .setFooter("Think you know it? Type `/guess` to submit your answer!");
        event.replyEmbeds(embed.build()).queue();
    }
}