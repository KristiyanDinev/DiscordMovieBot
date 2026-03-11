package me.kristiyan.moviebot.commands;

import me.kristiyan.moviebot.Main;
import me.kristiyan.moviebot.enums.LeaderboardTypeEnum;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        if (guild == null || guild.isDetached() || member == null) {
            return;
        }
        String guildId = guild.getId();
        String guildName = guild.getName();
        Main.serviceManager.getBotUsageService().addActivity(guildName, guildId,
                member.getId(), member.getEffectiveName(), event.getCommandString());
        boolean errorNotPremium = false;
        switch (event.getName()) {
            case "start-emoji":
            case "start-quote":
            case "daily-challenge":
            case "hint": {
                if (!Main.serviceManager.getPremiumService().isPremium(member.getId(), guild.getId())) {
                    event.reply("❌ This is premium feature only!").setEphemeral(true).queue();
                    errorNotPremium = true;
                }
            }
        }
        if (errorNotPremium) {
            return;
        }
        switch (event.getName()) {
            case "help" -> HelpCommand.handleHelpCommand(event);
            case "start" -> StartCommand.handleStartCommand(event, guildId, Main.serviceManager);
            case "start-emoji" -> StartCommand.handleStartEmojiCommand(event, guildId, Main.serviceManager);
            case "start-quote" -> StartCommand.handleStartQuoteCommand(event, guildId, Main.serviceManager);
            case "guess" -> GuessCommand.handleGuessCommand(event, guild, Main.serviceManager);
            case "leaderboard" -> LeaderboardCommand.handleLeaderboardCommand(event, guildId,
                    Main.serviceManager, LeaderboardTypeEnum.GLOBAL);
            case "leaderboard-daily" -> LeaderboardCommand.handleLeaderboardCommand(event, guildId,
                    Main.serviceManager, LeaderboardTypeEnum.DAILY);
            case "skip" -> SkipCommand.handleSkipCommand(event, guildId, Main.serviceManager);
            case "updates" -> UpdatesCommand.handleUpdatesCommand(event, Main.serviceManager);
            case "report"  -> ReportCommand.handleReportCommand(event, guildId, Main.serviceManager);
            case "ping"  -> PingCommand.handlePingCommand(event);
            case "hint"  -> HintCommand.handleHintCommand(event, guild, member, Main.serviceManager);
            case "daily-challenge"  -> DailyChallengeCommand.handleDailyChallengeCommand(event, Main.serviceManager);
        }
    }
}
