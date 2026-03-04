package me.kristiyan.moviebot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class PingCommand {

    public static void handlePingCommand(SlashCommandInteractionEvent event) {
        event.replyEmbeds(new EmbedBuilder()
                .setTitle("🎬 Guess The Movie: Ping")
                .addField("\uD83C\uDF10", event.getJDA().getGatewayPing()+" ms", false)
                .setColor(new Color(245, 197, 24))
                .setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl()).build()).queue();
    }
}
