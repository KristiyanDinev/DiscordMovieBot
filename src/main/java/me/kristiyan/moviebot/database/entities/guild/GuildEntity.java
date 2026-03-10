package me.kristiyan.moviebot.database.entities.guild;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "guilds",
        indexes = {
                @Index(name = "idx_guild_id", columnList = "guild_id", unique = true)
        }
)
public class GuildEntity {

    @Id
    @Column(name = "guild_id", nullable = false)
    private String guildId;

    @Column(name = "guild_name", nullable = false)
    private String guildName;

    @Column(name = "subscribed_at")
    private LocalDate subscribedAt;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    // Getters & Setters
    public String getGuildId() { return guildId; }
    public void setGuildId(String guildId) { this.guildId = guildId; }

    public String getGuildName() { return guildName; }
    public void setGuildName(String guildName) { this.guildName = guildName; }

    public LocalDate getSubscribedAt() { return subscribedAt; }
    public void setSubscribedAt(LocalDate subscribedAt) { this.subscribedAt = subscribedAt; }

    public LocalDate getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDate expiresAt) { this.expiresAt = expiresAt; }

    /** Safe null-check: never subscribed = not premium. */
    public boolean isPremium() {
        return expiresAt != null && expiresAt.isAfter(LocalDate.now());
    }
}