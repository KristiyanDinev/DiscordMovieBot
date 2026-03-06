package me.kristiyan.moviebot.commands;

import me.kristiyan.moviebot.database.entities.games.ActiveGameEntity;
import me.kristiyan.moviebot.database.entities.movie.MovieEntity;
import me.kristiyan.moviebot.enums.LeaderboardMemberStatus;
import me.kristiyan.moviebot.services.ServiceManager;
import me.kristiyan.moviebot.utils.MovieMatcher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;

public class GuessCommand {
    public static void handleGuessCommand(SlashCommandInteractionEvent event, Guild guild, ServiceManager serviceManager) {
        OptionMapping option = event.getOption("title");
        if (option == null) {
            event.reply("❌ Please provide a title!").setEphemeral(true).queue();
            return;
        }

        Member member = event.getMember();
        if (member == null) return;

        String guildId = guild.getId();
        String guildName = guild.getName();
        ActiveGameEntity activeGame = serviceManager.getActiveGameService().findRelevantGame(guildId, member.getId());

        if (activeGame == null) {
            event.reply("❌ No game is currently running for you. Start one with `/start`").setEphemeral(true).queue();
            return;
        }

        String guess = option.getAsString();
        MovieEntity movie = activeGame.getMovie();
        String type = movie.getContentType();
        String title = movie.getTitle();
        MessageChannelUnion channel = event.getChannel();
        Member selfMember = guild.getSelfMember();

        if (channel instanceof GuildChannel guildChannel) {
            if (!selfMember.hasPermission(guildChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)) {
                event.reply("❌ I don't have permission to see or chat in this channel!")
                        .queue();
                return;
            }
        }
        event.reply("\uD83E\uDD14 Calculating your guess...")
                .setEphemeral(true).queue();

        if (MovieMatcher.isMatch(guess, title)) {
            // Record attempt
            serviceManager.getActiveGameService().recordGuessAttempt(guildId, member.getId(), member.getEffectiveName(),
                    activeGame.getCurrentRound(), activeGame);

            int nextRound = activeGame.getCurrentRound() + 1;
            boolean isGameOver = (nextRound > activeGame.getTotalRounds());

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🎉 Correct!")
                    .setDescription("**" + member.getEffectiveName() + "** guessed it right!")
                    .addField(title, "was the "+type, false)
                    .addField(guess, "was their Guess", false)
                    .setColor(Color.GREEN);

            if (isGameOver) {
                embed.setFooter("Use `/start` to play another game! 🎬");

                // Update Leaderboard
                serviceManager.getLeaderboardsService().recordCorrectGuesses(guildId, guildName,
                        activeGame.isHighStakes() ? LeaderboardMemberStatus.TOP_GUESSES_HIGH_STAKES : LeaderboardMemberStatus.TOP_GUESSES);

                // Pass the SPECIFIC game entity to end it
                serviceManager.getActiveGameService().endGame(activeGame);

                channel.sendMessageEmbeds(embed.build()).queue();
            } else {
                activeGame.setCurrentRound(nextRound);
                MovieEntity nextMovie = serviceManager.getMovieService().getRandomMovie(type);
                activeGame.setMovie(nextMovie);
                serviceManager.getActiveGameService().updateActiveGame(activeGame);

                String description = "**Round " + nextRound + " of " + activeGame.getTotalRounds() + "**\n\nCan you guess this %s?".formatted(type);
                if (activeGame.isHighStakes()) description += "\n\nOne guess only!";

                String finalDesc = description;
                channel.sendMessageEmbeds(embed.build()).queue();
                channel.sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("🎬 Next Round!")
                        .setDescription(finalDesc)
                        .setColor(Color.BLUE)
                        .setImage(nextMovie.getGifUrl())
                        .setFooter("Good luck! 🍿").build()).queue();
            }
        } else {
            // WRONG GUESS
            // Note: Logic here implies Game Over on first wrong guess (High Stakes style logic usually)
            // If you want standard mode to allow multiple guesses, check !highStakes before ending.

            serviceManager.getLeaderboardsService().recordCorrectGuesses(guildId, guildName,
                    activeGame.isHighStakes() ? LeaderboardMemberStatus.TOP_GUESSES_HIGH_STAKES : LeaderboardMemberStatus.TOP_GUESSES);

            serviceManager.getActiveGameService().endGame(activeGame);

            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("❌ Wrong!")
                    .setDescription("**" + member.getEffectiveName() + "** guessed incorrectly!")
                    .addField(title, "was the "+type, false)
                    .addField(guess, "was their Guess", false)
                    .setFooter("Game Over! Start a new match with `/start` 🎬").build()).queue();
        }
    }
}
