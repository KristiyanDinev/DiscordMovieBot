package me.kristiyan.moviebot.commands;

import me.kristiyan.moviebot.database.entities.games.ActiveGameEntity;
import me.kristiyan.moviebot.database.entities.movie.MovieEntity;
import me.kristiyan.moviebot.enums.GamemodeEnum;
import me.kristiyan.moviebot.enums.LeaderboardMemberStatusEnum;
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
import java.util.Optional;

public class GuessCommand {
    public static void handleGuessCommand(SlashCommandInteractionEvent event, Guild guild, ServiceManager serviceManager) {
        OptionMapping option = event.getOption("title");
        if (option == null) {
            event.reply("❌ Please provide a title!").setEphemeral(true).queue();
            return;
        }
        OptionMapping gamemodeOption = event.getOption("gamemode");
        GamemodeEnum gamemode = gamemodeOption != null
                ? GamemodeEnum.valueOf(gamemodeOption.getAsString())
                : GamemodeEnum.NORMAL;

        Member member = event.getMember();
        if (member == null) return;

        String guildId = guild.getId();
        String guildName = guild.getName();
        ActiveGameEntity activeGame = serviceManager.getActiveGameService().findRelevantGame(guildId, member.getId(), gamemode);

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

        if (channel instanceof GuildChannel guildChannel &&
                !selfMember.hasPermission(guildChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)) {
            event.reply("❌ I don't have permission to see or chat in this channel!").queue();
            return;
        }
        event.reply("\uD83E\uDD14 Calculating your guess...")
                .setEphemeral(true).queue();

        if (MovieMatcher.isMatch(guess, title)) {
            // Record attempt
            serviceManager.getActiveGameService().recordGuessAttempt(guildId, member.getId(), member.getEffectiveName(),
                    activeGame.getCurrentRound(), activeGame);

            int nextRound = activeGame.getCurrentRound() + 1;
            boolean isGameOver = (nextRound > activeGame.getTotalRounds());

            if (gamemode == GamemodeEnum.DAILY_CHALLENGE) {
                serviceManager.getLeaderboardsService().recordCorrectGuesses(guildId, guildName,
                        activeGame.isHighStakes() ?
                                LeaderboardMemberStatusEnum.TOP_GUESSES_HIGH_STAKES :
                                LeaderboardMemberStatusEnum.TOP_GUESSES, gamemode);
                serviceManager.getActiveGameService().endGame(activeGame);
                channel.sendMessageEmbeds( new EmbedBuilder()
                        .setTitle("🎉 Daily Challenge — Correct!")
                        .setDescription("**" + member.getEffectiveName() + "** guessed it right!")
                        .addField(title, "was the "+type, false)
                        .setColor(Color.GREEN).build()).queue();
                return;
            }

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
                        activeGame.isHighStakes() ?
                                LeaderboardMemberStatusEnum.TOP_GUESSES_HIGH_STAKES :
                                LeaderboardMemberStatusEnum.TOP_GUESSES, gamemode);

                // Pass the SPECIFIC game entity to end it
                serviceManager.getActiveGameService().endGame(activeGame);

                channel.sendMessageEmbeds(embed.build()).queue();
            } else {
                activeGame.setCurrentRound(nextRound);
                Optional<MovieEntity> nextMovieOptional = serviceManager.getMovieService().getRandomMovie(type);
                if (nextMovieOptional.isEmpty()) {
                    channel.sendMessage("❌ Error! Can't get next movie. Please try again.").queue();
                    return;
                }
                MovieEntity nextMovie = nextMovieOptional.get();
                activeGame.setMovie(nextMovie);
                serviceManager.getActiveGameService().updateActiveGame(activeGame);

                String description = "**Round " + nextRound + " of " + activeGame.getTotalRounds() + "**\n\nCan you guess this %s?".formatted(type);
                if (activeGame.isHighStakes()) description += "\n\nOne guess only!";

                String finalDesc = description;
                channel.sendMessageEmbeds(embed.build()).queue();

                switch (gamemode) {
                    case EMOJI: {
                        channel.sendMessageEmbeds(new EmbedBuilder()
                                .setTitle("🎬 Next Round!")
                                .setDescription(finalDesc)
                                .addField("What movie is represented with these emojis?",
                                        nextMovie.getEmoji(), false)
                                .setColor(Color.BLUE)
                                .setFooter("Good luck! 🍿").build()).queue();
                        break;
                    }
                    case QUOTE: {
                        channel.sendMessageEmbeds(new EmbedBuilder()
                                .setTitle("🎬 Next Round!")
                                .setDescription(finalDesc)
                                .addField("In what movie was this a famous quite in?",
                                        nextMovie.getQuote(), false)
                                .setColor(Color.BLUE)
                                .setFooter("Good luck! 🍿").build()).queue();
                        break;
                    }
                    case NORMAL: {
                        // DEFAULT = NORMAL
                        channel.sendMessageEmbeds(new EmbedBuilder()
                                .setTitle("🎬 Next Round!")
                                .setDescription(finalDesc)
                                .setColor(Color.BLUE)
                                .setImage(nextMovie.getGifUrl())
                                .setFooter("Good luck! 🍿").build()).queue();
                        break;
                    }
                }
            }
        } else {
            if (gamemode == GamemodeEnum.DAILY_CHALLENGE) {
                channel.sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("❌ Daily Challenge — Wrong!")
                        .setDescription("**" + member.getEffectiveName() + "** guessed incorrectly!")
                        .setColor(Color.RED).build()).queue();
                return;
            }

            // WRONG GUESS
            // Note: Logic here implies Game Over on first wrong guess (High Stakes style logic usually)
            // If you want standard mode to allow multiple guesses, check !highStakes before ending.

            serviceManager.getLeaderboardsService().recordCorrectGuesses(guildId, guildName,
                    activeGame.isHighStakes() ?
                            LeaderboardMemberStatusEnum.TOP_GUESSES_HIGH_STAKES :
                            LeaderboardMemberStatusEnum.TOP_GUESSES, gamemode);

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
