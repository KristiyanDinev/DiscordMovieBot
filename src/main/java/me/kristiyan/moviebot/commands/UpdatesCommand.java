package me.kristiyan.moviebot.commands;

import me.kristiyan.moviebot.database.entities.updates.UpdateAnnouncementEntity;
import me.kristiyan.moviebot.services.ServiceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.List;

public class UpdatesCommand {
    public static void handleUpdatesCommand(SlashCommandInteractionEvent event, ServiceManager serviceManager) {
        List<UpdateAnnouncementEntity> updates = serviceManager.getUpdateAnnouncementService().getLatestUpdates(5);

        if (updates.isEmpty()) {
            event.reply("No updates found yet! Stay tuned for future movie updates. 🎬").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🆕 Latest Updates & Patch Notes")
                .setColor(Color.GREEN)
                .setDescription("Stay up to date with the latest features and fixes!")
                .setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

        for (UpdateAnnouncementEntity update : updates) {
            // Formatting the date (e.g., Feb 05, 2026)
            // title format: [Date] Update Title
            embed.addField(String.format("📅 `%s` — %s", update.getReleasedAt().toString(), update.getTitle()),
                    update.getShortDescription(), false);
        }

        embed.setFooter("Thank you for being part of our community! ❤️");
        event.replyEmbeds(embed.build()).queue();
    }
}
