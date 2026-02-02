package me.kristiyan.moviebot;

import me.kristiyan.moviebot.commands.CommandListener;
import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.services.ServiceManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Main {
    public static ServiceManager serviceManager;

    public static void main(String[] args) {

        try {
            // Initialize database
            DatabaseManager databaseManager = new DatabaseManager();
            Runtime.getRuntime().addShutdownHook(new Thread(databaseManager::shutdown));
            serviceManager = new ServiceManager(databaseManager);

            // Create JDA instance with minimal required intents
            // TESTING_DISCORD_BOT_TOKEN
            // DISCORD_MOVIE_BOT_TOKEN
            JDA jda = JDABuilder.createDefault(System.getenv("TESTING_DISCORD_BOT_TOKEN"))
                    .setActivity(Activity.playing("Movie Guessing Game! Use /start"))
                    .addEventListeners(new CommandListener())
                    .build();

            // Wait for JDA to be ready
            jda.awaitReady();
            OptionData typeOption = new OptionData(OptionType.STRING, "type", "Choose the movie type", true)
                    .addChoice("Movie", "movie")
                    .addChoice("Anime", "anime");

            // Register slash commands
            jda.updateCommands().addCommands(
                    Commands.slash("start", "Start guessing movies to your liking")
                            .addOption(OptionType.INTEGER, "rounds",
                                    "How many correct guesses can you get?",
                                    false)
                            .addOption(OptionType.BOOLEAN, "high_stakes",
                                    "Do you want to have only one chance to guess the movie right?",
                                    false)
                            .addOption(OptionType.STRING, "type", "Choose between Movies or Anime", true)
                            .addOptions(typeOption),


                    Commands.slash("guess", "Guess the movie title")
                            .addOption(OptionType.STRING, "title", "Your guess for the movie title", true),

                    Commands.slash("leaderboard", "Show the server and member leaderboards"),
                    Commands.slash("skip", "Skip the current movie and start a new round (reveals the answer)"),
                    Commands.slash("help", "Displays the help menu")
            ).queue();

            System.out.println("Guess The Movie Bot is ready!");

        } catch (Exception e) {
            System.err.println("Failed to start bot: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
