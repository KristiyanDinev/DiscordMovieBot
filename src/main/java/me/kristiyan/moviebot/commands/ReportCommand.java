package me.kristiyan.moviebot.commands;

import me.kristiyan.moviebot.database.entities.games.ActiveGameEntity;
import me.kristiyan.moviebot.database.entities.movie.MovieEntity;
import me.kristiyan.moviebot.services.ServiceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;

public class ReportCommand {
    public static void handleReportCommand(SlashCommandInteractionEvent event, String guildId, ServiceManager serviceManager) {
        Member member = event.getMember();
        if (member == null) {
            return;
        }
        OptionMapping reasonOpt = event.getOption("reason");
        if (reasonOpt == null) {
            event.reply("❌ You need to provide a reason why!").setEphemeral(true).queue();
            return;
        }
        String reason = reasonOpt.getAsString();
        if (reason.length() > 100) {
            event.reply("❌ Sorry, but please be more brief with the reason.").setEphemeral(true).queue();
            return;
        }
        String memberId = member.getId();

        // Find the relevant game to know which movie is being reported
        ActiveGameEntity activeGame = serviceManager.getActiveGameService().findRelevantGame(guildId, memberId);

        if (activeGame == null) {
            event.reply("❌ You can only report a movie during an active game!").setEphemeral(true).queue();
            return;
        }

        MovieEntity currentMovie = activeGame.getMovie();

        if (serviceManager.getMovieReportService().hasAlreadyReported(memberId, currentMovie)) {
            event.reply("⚠️ You have already reported this movie! Our team is looking into it.")
                    .setEphemeral(true).queue();
            return;
        }

        serviceManager.getMovieReportService().submitReport(event.getJDA(), memberId, member.getEffectiveName(),
                currentMovie, reason);

        event.replyEmbeds(new EmbedBuilder()
                .setTitle("✅ Report Submitted")
                .setDescription("Thank you! You've reported **" + currentMovie.getTitle() + "** for inaccuracy.")
                .addField("Reason", reason, false)
                .setColor(Color.YELLOW)
                .setFooter("We appreciate your help in keeping the bot accurate!")
                .build()).setEphemeral(true).queue();


    }
}
