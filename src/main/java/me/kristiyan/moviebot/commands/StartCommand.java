package me.kristiyan.moviebot.commands;

import me.kristiyan.moviebot.database.entities.movie.MovieEntity;
import me.kristiyan.moviebot.enums.GamemodeEnum;
import me.kristiyan.moviebot.services.ServiceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;
import java.util.Optional;

public class StartCommand {

    public static void handleStartCommand(SlashCommandInteractionEvent event, String guildId,
                                          ServiceManager serviceManager) {
        Member member = event.getMember();
        if (member == null) return;

        // Parse options first to know what to check
        OptionMapping typeMapping = event.getOption("type");
        OptionMapping roundsMapping = event.getOption("rounds");
        OptionMapping highStakesMapping = event.getOption("high_stakes");
        OptionMapping lockedMapping = event.getOption("locked");

        boolean locked = lockedMapping != null && lockedMapping.getAsBoolean();

        // Context-Aware Check
        if (locked) {
            if (serviceManager.getActiveGameService().hasActiveLockedGame(guildId, member.getId())) {
                event.reply("⚠️ You already have a locked game running! Finish it or use `/skip`.").setEphemeral(true).queue();
                return;
            }
        } else {
            if (serviceManager.getActiveGameService().hasActivePublicGame(guildId, GamemodeEnum.NORMAL)) {
                event.reply("⚠️ There is already a public movie guess in progress! Join in with `/guess`.").setEphemeral(true).queue();
                return;
            }
        }

        // Prepare Game Settings
        String type = (typeMapping != null && "anime".equals(typeMapping.getAsString())) ? "anime" : "movie";
        int totalRounds = (roundsMapping != null && roundsMapping.getAsInt() != 1) ? roundsMapping.getAsInt() : 1;
        boolean highStakes = highStakesMapping != null && highStakesMapping.getAsBoolean();

        Optional<MovieEntity> movieEntityOptional = serviceManager.getMovieService().getRandomMovie(type);
        if (movieEntityOptional.isEmpty()) {
            event.reply("❌ Could not retrieve movie information.").setEphemeral(true).queue();
            return;
        }
        MovieEntity movieEntity = movieEntityOptional.get();

        if (!serviceManager.getActiveGameService().startGame(guildId, movieEntity, totalRounds,
                highStakes, locked, member.getId(), GamemodeEnum.NORMAL)) {
            event.reply("❌ Could not start the game.").setEphemeral(true).queue();
            return;
        }

        String description = "**Round 1 of " + totalRounds + "**\n\nCan you guess this %s? Use `/guess <title>`!".formatted(type);
        if (highStakes) {
            description += totalRounds == 1 ? "\n\n🔥 High Stakes: One guess only!" : "\n\n🔥 High Stakes: One guess per round!";
        }

        String title = locked ? "🎬 Guess The Movie! (Locked Mode)" : "🎬 Guess The Movie!";

        event.replyEmbeds(new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(locked ? Color.MAGENTA : Color.BLUE)
                .setImage(movieEntity.getGifUrl())
                .setFooter("Good luck! 🍿").build()).queue();
    }

    public static void handleStartEmojiCommand(SlashCommandInteractionEvent event, String guildId,
                                         ServiceManager serviceManager) {
        Member member = event.getMember();
        if (member == null) return;

        // Parse options first to know what to check
        OptionMapping typeMapping = event.getOption("type");
        OptionMapping roundsMapping = event.getOption("rounds");
        OptionMapping highStakesMapping = event.getOption("high_stakes");
        OptionMapping lockedMapping = event.getOption("locked");

        boolean locked = lockedMapping != null && lockedMapping.getAsBoolean();

        // Context-Aware Check
        if (locked) {
            if (serviceManager.getActiveGameService().hasActiveLockedGame(guildId, member.getId())) {
                event.reply("⚠️ You already have a locked game running! Finish it or use `/skip`.").setEphemeral(true).queue();
                return;
            }
        } else {
            if (serviceManager.getActiveGameService().hasActivePublicGame(guildId, GamemodeEnum.EMOJI)) {
                event.reply("⚠️ There is already a public movie guess in progress! Join in with `/guess`.").setEphemeral(true).queue();
                return;
            }
        }

        // Prepare Game Settings
        String type = (typeMapping != null && "anime".equals(typeMapping.getAsString())) ? "anime" : "movie";
        int totalRounds = (roundsMapping != null && roundsMapping.getAsInt() != 1) ? roundsMapping.getAsInt() : 1;
        boolean highStakes = highStakesMapping != null && highStakesMapping.getAsBoolean();

        Optional<MovieEntity> movieEntityOptional = serviceManager.getMovieService().getRandomMovie(type);
        if (movieEntityOptional.isEmpty()) {
            event.reply("❌ Could not retrieve movie information.").setEphemeral(true).queue();
            return;
        }
        MovieEntity movieEntity = movieEntityOptional.get();
        if (!serviceManager.getActiveGameService().startGame(guildId, movieEntity, totalRounds, highStakes,
                locked, member.getId(), GamemodeEnum.EMOJI)) {
            event.reply("❌ Could not start the game.").setEphemeral(true).queue();
            return;
        }

        String description = "**Round 1 of " + totalRounds + "**\n\nCan you guess this %s? Use `/guess <title>`!".formatted(type);
        if (highStakes) {
            description += totalRounds == 1 ? "\n\n🔥 High Stakes: One guess only!" : "\n\n🔥 High Stakes: One guess per round!";
        }

        String title = locked ? "🎬 Guess The Movie! (Locked Mode)" : "🎬 Guess The Movie!";

        event.replyEmbeds(new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .addField("What movie is represented with these emojis?", movieEntity.getEmoji(), false)
                .setColor(locked ? Color.MAGENTA : Color.BLUE)
                .setFooter("Good luck! 🍿").build()).queue();
    }

    public static void handleStartQuoteCommand(SlashCommandInteractionEvent event, String guildId,
                                               ServiceManager serviceManager) {
        Member member = event.getMember();
        if (member == null) return;

        // Parse options first to know what to check
        OptionMapping typeMapping = event.getOption("type");
        OptionMapping roundsMapping = event.getOption("rounds");
        OptionMapping highStakesMapping = event.getOption("high_stakes");
        OptionMapping lockedMapping = event.getOption("locked");

        boolean locked = lockedMapping != null && lockedMapping.getAsBoolean();

        // Context-Aware Check
        if (locked) {
            if (serviceManager.getActiveGameService().hasActiveLockedGame(guildId, member.getId())) {
                event.reply("⚠️ You already have a locked game running! Finish it or use `/skip`.").setEphemeral(true).queue();
                return;
            }
        } else {
            if (serviceManager.getActiveGameService().hasActivePublicGame(guildId, GamemodeEnum.QUOTE)) {
                event.reply("⚠️ There is already a public movie guess in progress! Join in with `/guess`.").setEphemeral(true).queue();
                return;
            }
        }

        // Prepare Game Settings
        String type = (typeMapping != null && "anime".equals(typeMapping.getAsString())) ? "anime" : "movie";
        int totalRounds = (roundsMapping != null && roundsMapping.getAsInt() != 1) ? roundsMapping.getAsInt() : 1;
        boolean highStakes = highStakesMapping != null && highStakesMapping.getAsBoolean();
        Optional<MovieEntity> movieEntityOptional = serviceManager.getMovieService().getRandomMovie(type);
        if (movieEntityOptional.isEmpty()) {
            event.reply("❌ Could not retrieve movie information.").setEphemeral(true).queue();
            return;
        }
        MovieEntity movieEntity = movieEntityOptional.get();
        if (!serviceManager.getActiveGameService().startGame(guildId, movieEntity, totalRounds,
                highStakes, locked, member.getId(), GamemodeEnum.QUOTE)) {
            event.reply("❌ Could not start the game.").setEphemeral(true).queue();
            return;
        }

        String description = "**Round 1 of " + totalRounds + "**\n\nCan you guess this %s? Use `/guess <title>`!".formatted(type);
        if (highStakes) {
            description += totalRounds == 1 ? "\n\n🔥 High Stakes: One guess only!" : "\n\n🔥 High Stakes: One guess per round!";
        }

        String title = locked ? "🎬 Guess The Movie! (Locked Mode)" : "🎬 Guess The Movie!";
        event.replyEmbeds(new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(locked ? Color.MAGENTA : Color.BLUE)
                .addField("In what movie was this a famous quite in?",  movieEntity.getQuote(), false)
                .setFooter("Good luck! 🍿").build()).queue();
    }
}
