package me.kristiyan.moviebot.commands;

import me.kristiyan.moviebot.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            return;
        }
        String guildId = guild.getId();
        switch (event.getName()) {
            case "help" -> HelpCommand.handleHelpCommand(event);
            case "start" -> StartCommand.handleStartCommand(event, guildId, Main.serviceManager);
            case "guess" -> GuessCommand.handleGuessCommand(event, guildId, guild.getName(), Main.serviceManager);
            case "leaderboard" -> LeaderboardCommand.handleLeaderboardCommand(event, guildId, Main.serviceManager);
            case "skip" -> SkipCommand.handleSkipCommand(event, guildId, Main.serviceManager);
            case "updates" -> UpdatesCommand.handleUpdatesCommand(event, Main.serviceManager);
            case "report"  -> ReportCommand.handleReportCommand(event, guildId, Main.serviceManager);
        }
    }
}
