package me.kristiyan.moviebot;

import me.kristiyan.moviebot.commands.CommandListener;
import me.kristiyan.moviebot.database.DatabaseManager;
import me.kristiyan.moviebot.enums.GamemodeEnum;
import me.kristiyan.moviebot.listeners.EntitlementListener;
import me.kristiyan.moviebot.services.ServiceManager;
import me.kristiyan.moviebot.tests.TestMain;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
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
                    .addEventListeners(new EntitlementListener(serviceManager.getPremiumService()))
                    .build();

            TestMain testMain = new TestMain(databaseManager, serviceManager, jda);

            // Wait for JDA to be ready
            jda.awaitReady();
            serviceManager.getMovieReportService().setTextChannel(jda, System.getenv("REPORT_GUILD_ID"),
                    System.getenv("REPORT_CHANNEL_ID"));

            testMain.testMovieReport();

            OptionData typeOption = new OptionData(OptionType.STRING, "type", "Choose the movie type", false)
                    .addChoice("Movie", "movie")
                    .addChoice("Anime", "anime");

            // Create choices from your enum
            List<Command.Choice> choices = Arrays.stream(GamemodeEnum.values())
                    .map(mode -> new Command.Choice(mode.name(), mode.name()))
                    .toList();

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

                    Commands.slash("start-emoji",
                                    "Start a game where you have to guess the movie from emojis. Premium only!")
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

                    Commands.slash("start-quote",
                                    "Start a game where you have to guess the movie from a famous quote. Premium only!")
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

                    Commands.slash("start-zoomed",
                                    "Start a new game where you have to guess the movie from a zoomed in scene. Premium only!")
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

                    Commands.slash("daily-challenge", "Try to guess today's movie by clues. Premium only!"),

                Commands.slash("guess", "Guess the movie title")
                        .addOption(OptionType.STRING, "title", "Your guess for the movie title", true)
                        .addOptions(new OptionData(OptionType.STRING, "gamemode", "Select a gamemode", false)
                                .addChoices(choices)),

                    Commands.slash("leaderboard", "Show the server and member global leaderboards"),
                    Commands.slash("leaderboard-daily", "Show the server and member daily leaderboards"),
                    Commands.slash("skip", "Skip the current movie and start a new round (reveals the answer)")
                            .addOptions(new OptionData(OptionType.STRING, "gamemode", "Select a gamemode", false)
                                    .addChoices(choices)),
                    Commands.slash("help", "Displays the help menu"),
                    Commands.slash("ping", "Displays the latency of the bot"),
                    Commands.slash("updates", "See what's new in the latest version"),
                    Commands.slash("hint", "Shows you useful hints to help your thinking. Premium only!"),
                    Commands.slash("report", "Report an inaccurate movie for your current game")
                            .addOption(OptionType.STRING, "reason",
                                    "The is the reason of your report? How was the movie inaccurate?",
                                    true)
            ).queue();
            System.out.println(jda.getSelfUser().getName()+" is ready!");

        } catch (Exception e) {
            System.err.println("Failed to start bot: " + e.getMessage());
            logger.error("Error fetching daily guild leaderboard: ", e);
        }
    }
}
