package me.kristiyan.moviebot;

import me.kristiyan.moviebot.commands.CommandListener;
import me.kristiyan.moviebot.db.DatabaseManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Main {
    public static void main(String[] args) {

        try {
            // Initialize database
            DatabaseManager.initialize();

            // Create JDA instance with minimal required intents
            JDA jda = JDABuilder.createDefault(System.getenv("DISCORD_MOVIE_BOT_TOKEN"))
                    .setActivity(Activity.playing("Movie Guessing Game! Use /start"))
                    .addEventListeners(new CommandListener())
                    .build();

            // Wait for JDA to be ready
            jda.awaitReady();

            // Register slash commands
            jda.updateCommands().addCommands(
                    Commands.slash("start", "Start a new movie guessing round with a GIF!"),
                    Commands.slash("guess", "Guess the movie title (one attempt only!)")
                            .addOption(OptionType.STRING, "title", "Your guess for the movie title", true),
                    Commands.slash("leaderboard", "Show the server and member leaderboards"),
                    Commands.slash("skip", "Skip the current movie and start a new round (reveals the answer)")
            ).queue();

            System.out.println("Movie Guess Bot is ready!");

        } catch (Exception e) {
            System.err.println("Failed to start bot: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
