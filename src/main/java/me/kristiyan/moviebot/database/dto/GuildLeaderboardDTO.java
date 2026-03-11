package me.kristiyan.moviebot.database.dto;

import me.kristiyan.moviebot.enums.LeaderboardMemberStatusEnum;

public class GuildLeaderboardDTO {
    private String guildId;
    private String guildName;
    private int totalCorrectGuesses;
    private LeaderboardMemberStatusEnum statusType;

    // Getters & Setters
    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public String getGuildName() {
        return guildName;
    }

    public void setGuildName(String guildName) {
        this.guildName = guildName;
    }

    public int getTotalCorrectGuesses() {
        return totalCorrectGuesses;
    }

    public void setTotalCorrectGuesses(int totalCorrectGuesses) {
        this.totalCorrectGuesses = totalCorrectGuesses;
    }

    public LeaderboardMemberStatusEnum getStatusType() {
        return statusType;
    }

    public void setStatusType(LeaderboardMemberStatusEnum statusType) {
        this.statusType = statusType;
    }

    @Override
    public String toString() {
        return String.format("Guild: %s (ID: %s) - %d correct guesses [%s]",
                guildName, guildId, totalCorrectGuesses, statusType);
    }
}