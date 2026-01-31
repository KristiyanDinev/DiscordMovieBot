package me.kristiyan.moviebot.commands;

import me.kristiyan.moviebot.db.DatabaseManager;
import me.kristiyan.moviebot.utils.MovieMatcher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;

public class GuessCommand {
    public static void handleGuessCommand(SlashCommandInteractionEvent event, String guildId, String guildName) {
        // Get the user's guess
        OptionMapping option = event.getOption("title");
        if (option == null) {
            event.reply("❌ Please provide a movie title!").setEphemeral(true).queue();
            return;
        }

        String guess = option.getAsString();

        // Check if there's an active movie
        DatabaseManager.ActiveMovie activeMovie = DatabaseManager.getActiveMovie(guildId);

        if (activeMovie == null) {
            event.reply("⚠️ No active movie to guess! Use `/start` to begin a new round.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get member info
        Member member = event.getMember();
        if (member == null) {
            event.reply("❌ Could not retrieve member information.").setEphemeral(true).queue();
            return;
        }

        String memberId = member.getId();
        String memberNickname = member.getEffectiveName();

        // Check if this member has already guessed
        if (DatabaseManager.hasAlreadyGuessed(guildId, memberId)) {
            event.reply("⚠️ You've already made your guess for this movie! Wait for someone else to guess correctly or use `/skip` to start a new round.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Record that this member has now made their guess
        DatabaseManager.recordGuessAttempt(guildId, memberId);

        // Check if the guess is correct using fuzzy matching
        if (MovieMatcher.isMatch(guess, activeMovie.title)) {
            // Record the correct guess
            DatabaseManager.recordCorrectGuess(guildId, guildName, memberId, memberNickname);

            // Clear the active movie and all guesses
            DatabaseManager.clearActiveMovie(guildId);

            // Send success message (public so everyone can see)
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("🎉 Correct!")
                    .setDescription("**" + memberNickname + "** guessed it right!")
                    .addField("Movie", activeMovie.title, false)
                    .addField("Their Guess", guess, false)
                    .setColor(Color.GREEN)
                    .setFooter("Use /start to play another round! 🎬").build()).queue();
        } else {
            // Wrong guess - reveal the answer and end the round
            DatabaseManager.clearActiveMovie(guildId);

            // Send failure message with the answer (public so everyone sees)
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("❌ Wrong!")
                    .setDescription("**" + memberNickname + "** guessed incorrectly!\n\nThe movie was: **" + activeMovie.title + "**")
                    .addField("Their Guess", guess, false)
                    .setColor(Color.RED)
                    .setFooter("Better luck next time! Use /start for a new round 🎬").build()).queue();
        }
    }
}
