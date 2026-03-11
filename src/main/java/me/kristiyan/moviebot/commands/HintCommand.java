package me.kristiyan.moviebot.commands;

import me.kristiyan.moviebot.database.entities.games.ActiveGameEntity;
import me.kristiyan.moviebot.database.entities.movie.MovieEntity;
import me.kristiyan.moviebot.enums.GamemodeEnum;
import me.kristiyan.moviebot.services.ServiceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;
import java.util.Optional;

public class HintCommand {

    public static void handleHintCommand(SlashCommandInteractionEvent event, Guild guild, Member member,
                                         ServiceManager serviceManager) {
        String guildId = guild.getId();
        OptionMapping gamemodeMapping = event.getOption("gamemode");
        GamemodeEnum gamemode = (gamemodeMapping != null)
                ? GamemodeEnum.valueOf(gamemodeMapping.getAsString().toUpperCase())
                : GamemodeEnum.NORMAL;

        Optional<ActiveGameEntity> movieEntityOptional = serviceManager.getActiveGameService()
                .getActiveGameEntity(guildId, gamemode);
        if (movieEntityOptional.isEmpty()) {
            event.reply("⚠️ No active game found for this gamemode!").setEphemeral(true).queue();
            return;
        }
        event.replyEmbeds(new EmbedBuilder()
                .setTitle("💡 Hint")
                .setDescription(movieEntityOptional.get().getMovie().getHint())
                .setColor(Color.YELLOW)
                .setFooter("Use `/guess <title>` to make your guess! 🍿")
                .build()
        ).setEphemeral(true).queue();
    }
}