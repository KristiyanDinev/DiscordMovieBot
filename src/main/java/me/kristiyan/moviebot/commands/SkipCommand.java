package me.kristiyan.moviebot.commands;

import me.kristiyan.moviebot.database.entities.games.ActiveGameEntity;
import me.kristiyan.moviebot.database.entities.movie.MovieEntity;
import me.kristiyan.moviebot.services.ServiceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class SkipCommand {
    public static void handleSkipCommand(SlashCommandInteractionEvent event, String guildId,
                                         ServiceManager serviceManager) {
        Member member = event.getMember();
        if (member == null) return;

        // INTELLIGENT LOOKUP
        ActiveGameEntity activeGameEntity = serviceManager.getActiveGameService().findRelevantGame(guildId, member.getId());
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
        MovieEntity movieEntity = serviceManager.getMovieService().getRandomMovie(type);
        if (movieEntity == null) {
            event.reply("❌ Error retrieving new movie.").setEphemeral(true).queue();
            return;
        }

        // Update DB
        activeGameEntity.setMovie(movieEntity);
        serviceManager.getActiveGameService().updateActiveGame(activeGameEntity);

        event.replyEmbeds(new EmbedBuilder()
                .setTitle("⏭️ Movie Skipped!")
                .setDescription("The previous " + type + " was: **" + prevMovieEntity.getTitle() + "**\n\nHere's a new one!")
                .setImage(movieEntity.getGifUrl())
                .setColor(Color.CYAN)
                .setFooter("Use `/guess <title>` to make your guess! 🍿").build()).queue();
    }
}
