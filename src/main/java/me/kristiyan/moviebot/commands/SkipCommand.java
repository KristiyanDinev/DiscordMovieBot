package me.kristiyan.moviebot.commands;

import me.kristiyan.moviebot.database.entities.games.ActiveGameEntity;
import me.kristiyan.moviebot.database.entities.movie.MovieEntity;
import me.kristiyan.moviebot.enums.GamemodeEnum;
import me.kristiyan.moviebot.services.ServiceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;
import java.util.Optional;

public class SkipCommand {
    public static void handleSkipCommand(SlashCommandInteractionEvent event, String guildId,
                                         ServiceManager serviceManager) {
        Member member = event.getMember();
        if (member == null) return;

        OptionMapping gamemodeOption = event.getOption("gamemode");
        GamemodeEnum gamemode = gamemodeOption != null
                ? GamemodeEnum.valueOf(gamemodeOption.getAsString())
                : GamemodeEnum.NORMAL;

        // INTELLIGENT LOOKUP
        ActiveGameEntity activeGameEntity = serviceManager.getActiveGameService().findRelevantGame(guildId, member.getId(),
                gamemode);
        if (activeGameEntity == null) {
            event.reply("⚠️ No active movie to skip!").setEphemeral(true).queue();
            return;
        }

        if (activeGameEntity.isHighStakes()) {
            event.reply("❌ Sorry. You can't skip this round. It's High Stakes Mode 🔥").setEphemeral(true).queue();
            return;
        }

        MovieEntity prevMovieEntity = activeGameEntity.getMovie();
        String type = prevMovieEntity.getContentType();

        // Fetch new movie
        Optional<MovieEntity> movieEntityOptional = serviceManager.getMovieService().getRandomMovie(type);
        if (movieEntityOptional.isEmpty()) {
            event.reply("❌ Error retrieving new movie.").setEphemeral(true).queue();
            return;
        }
        MovieEntity movieEntity = movieEntityOptional.get();

        // Update DB
        activeGameEntity.setMovie(movieEntity);
        serviceManager.getActiveGameService().updateActiveGame(activeGameEntity);
        String description = "The previous " + type + " was: **" + prevMovieEntity.getTitle() + "**\n\nHere's a new one!";
        String title = "⏭️ Movie Skipped!";
        String footer = "Use `/guess <title>` to make your guess! 🍿";
        switch (gamemode) {
            case EMOJI: {
                event.replyEmbeds(new EmbedBuilder()
                        .setTitle(title)
                        .setDescription(description)
                        .addField("What movie is represented with these emojis?",
                                movieEntity.getEmoji(), false)
                        .setColor(Color.CYAN)
                        .setFooter(footer).build()).queue();
                break;
            }
            case QUOTE: {
                event.replyEmbeds(new EmbedBuilder()
                        .setTitle(title)
                        .setDescription(description)
                        .addField("In what movie was this a famous quite in?",
                                movieEntity.getQuote(), false)
                        .setColor(Color.CYAN)
                        .setFooter(footer).build()).queue();
                break;
            }
            default: {
                // DEFAULT = NORMAL
                event.replyEmbeds(new EmbedBuilder()
                        .setTitle(title)
                        .setDescription(description)
                        .setColor(Color.CYAN)
                        .setImage(movieEntity.getGifUrl())
                        .setFooter(footer).build()).queue();
                break;
            }
        }
    }
}
