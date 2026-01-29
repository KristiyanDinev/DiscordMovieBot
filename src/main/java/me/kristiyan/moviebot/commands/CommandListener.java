package me.kristiyan.moviebot.commands;

import me.kristiyan.moviebot.db.DatabaseManager;
import me.kristiyan.moviebot.utils.MovieMatcher;
import me.kristiyan.moviebot.utils.MovieProvider;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.Color;
import java.util.List;

public class CommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            return;
        }
        String guildId = guild.getId();
        String guildName = guild.getName();

        switch (event.getName()) {
            case "help" -> handleHelpCommand(event);
            case "start" -> handleStartCommand(event, guildId);
            case "guess" -> handleGuessCommand(event, guildId, guildName);
            case "leaderboard" -> handleLeaderboardCommand(event, guildId);
            case "skip" -> handleSkipCommand(event, guildId);
        }
    }

    public void handleHelpCommand(SlashCommandInteractionEvent event) {
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

    private void handleStartCommand(SlashCommandInteractionEvent event, String guildId) {
        // Check if there's already an active movie
        if (DatabaseManager.getActiveMovie(guildId) != null) {
            event.reply("⚠️ There's already an active movie to guess! Use `/guess <title>` to make your guess (one attempt only!), or `/skip` to reveal the answer and start a new round.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get a random movie
        MovieProvider.Movie movie = MovieProvider.getRandomMovie();

        // Store it in the database
        DatabaseManager.setActiveMovie(guildId, movie.title, movie.gifUrl);

        // Send the GIF with a prompt
        event.replyEmbeds(new EmbedBuilder()
                .setTitle("🎬 Movie Guessing Game!")
                .setDescription("Can you guess this movie? Use `/guess <title>` to make your guess!\n\n⚠️ **You only get ONE attempt!**")
                .setColor(Color.BLUE)
                .setImage(movie.gifUrl)
                .setFooter("Good luck! 🍿").build()).queue();
    }

    private void handleGuessCommand(SlashCommandInteractionEvent event, String guildId, String guildName) {
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
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🎉 Correct!")
                    .setDescription("**" + memberNickname + "** guessed it right!")
                    .addField("Movie", activeMovie.title, false)
                    .addField("Their Guess", guess, false)
                    .setColor(Color.GREEN)
                    .setFooter("Use /start to play another round! 🎬");

            event.replyEmbeds(embed.build()).queue();
        } else {
            // Wrong guess - reveal the answer and end the round
            DatabaseManager.clearActiveMovie(guildId);

            // Send failure message with the answer (public so everyone sees)
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("❌ Wrong!")
                    .setDescription("**" + memberNickname + "** guessed incorrectly!\n\nThe movie was: **" + activeMovie.title + "**")
                    .addField("Their Guess", guess, false)
                    .setColor(Color.RED)
                    .setFooter("Better luck next time! Use /start for a new round 🎬");

            event.replyEmbeds(embed.build()).queue();
        }
    }

    private void handleLeaderboardCommand(SlashCommandInteractionEvent event, String guildId) {
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

    private void handleSkipCommand(SlashCommandInteractionEvent event, String guildId) {
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
        MovieProvider.Movie movie = MovieProvider.getRandomMovie();

        // Store it in the database
        DatabaseManager.setActiveMovie(guildId, movie.title, movie.gifUrl);

        // Send message revealing the previous answer and showing new movie
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("⏭️ Movie Skipped!")
                .setDescription("The previous movie was: **" + activeMovie.title + "**\n\nHere's a new one! Can you guess it?\n\n⚠️ **Remember: Only ONE attempt!**")
                .setImage(movie.gifUrl)
                .setColor(Color.ORANGE)
                .setFooter("Use /guess <title> to make your guess! 🍿");

        event.replyEmbeds(embed.build()).queue();
    }
}
