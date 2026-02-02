package me.kristiyan.moviebot.database.entities.leaderboards;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "guild_leaderboard")
public class GuildLeaderboardEntity {

    @Id
    @Column(name = "guild_id")
    private String guildId;

    @Column(name = "guild_name", nullable = false)
    private String guildName;

    @Column(name = "correct_guesses", nullable = false)
    private int correctGuesses = 0;

    // Getters & Setters
    public String getGuildId() { return guildId; }
    public void setGuildId(String guildId) { this.guildId = guildId; }

    public String getGuildName() { return guildName; }
    public void setGuildName(String guildName) { this.guildName = guildName; }

    public int getCorrectGuesses() { return correctGuesses; }
    public void setCorrectGuesses(int correctGuesses) { this.correctGuesses = correctGuesses; }
}
