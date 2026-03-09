package me.kristiyan.moviebot.database.entities.leaderboards;

import jakarta.persistence.*;

@Entity
@Table(name = "guilds",
        indexes = {
                @Index(name = "idx_guild_id", columnList = "guild_id", unique = true)
        }
)
public class GuildEntity {
    @Id
    @Column(name = "guild_id", nullable = false, length = 20)
    private String guildId;

    @Column(name = "guild_name", nullable = false, length = 100)
    private String guildName;

    // Getters & Setters
    public String getGuildId() { return guildId; }
    public void setGuildId(String guildId) { this.guildId = guildId; }

    public String getGuildName() { return guildName; }
    public void setGuildName(String guildName) { this.guildName = guildName; }
}
