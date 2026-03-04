package me.kristiyan.moviebot;

import me.kristiyan.moviebot.commands.CommandListener;
import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.services.ServiceManager;
import me.kristiyan.moviebot.tests.TestMain;
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
            JDA jda = JDABuilder.createDefault(System.getenv("DISCORD_MOVIE_BOT_TOKEN"))
                    .setActivity(Activity.playing("Movie Guessing Game! Use /start"))
                    .addEventListeners(new CommandListener())
                    .build();

            //TestMain testMain = new TestMain(databaseManager, serviceManager, jda);

            // Wait for JDA to be ready
            jda.awaitReady();
            serviceManager.getMovieReportService().setTextChannel(jda, System.getenv("REPORT_GUILD_ID"),
                    System.getenv("REPORT_CHANNEL_ID"));

            //testMain.testMovieReport();

            OptionData typeOption = new OptionData(OptionType.STRING, "type", "Choose the movie type", false)
                    .addChoice("Movie", "movie")
                    .addChoice("Anime", "anime");

            // Register slash commands
            jda.updateCommands().addCommands(
                    Commands.slash("start", "Start guessing movies to your liking")
                            .addOptions(typeOption)
                            .addOption(OptionType.INTEGER, "rounds",
                                    "How many correct guesses can you get?",
                                    false)
                            .addOption(OptionType.BOOLEAN, "high_stakes",
                                    "Do you want to have only one chance to guess the movie right?",
                                    false)
                    .addOption(OptionType.BOOLEAN, "locked",
                            "Do you want to play solo?",
                            false),


                    Commands.slash("guess", "Guess the movie title")
                            .addOption(OptionType.STRING, "title", "Your guess for the movie title", true),

                    Commands.slash("leaderboard", "Show the server and member leaderboards"),
                    Commands.slash("skip", "Skip the current movie and start a new round (reveals the answer)"),
                    Commands.slash("help", "Displays the help menu"),
                    Commands.slash("ping", "Displays the latency of the bot"),
                    Commands.slash("updates", "See what's new in the latest version"),
                    Commands.slash("report", "Report an inaccurate movie for your current game")
                            .addOption(OptionType.STRING, "reason",
                                    "The is the reason of your report? How was the movie inaccurate?",
                                    true)
            ).queue();

            System.out.println(jda.getSelfUser().getName()+" is ready!");

        } catch (Exception e) {
            System.err.println("Failed to start bot: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
