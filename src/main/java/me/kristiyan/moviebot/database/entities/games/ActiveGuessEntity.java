package me.kristiyan.moviebot.database.entities.games;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "active_guesses", indexes = {
        @Index(name = "idx_active_guesses_round", columnList = "guild_id, round_number")
})
public class ActiveGuessEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guild_id", nullable = false)
    private String guildId;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @Column(name = "guessed_at")
    private LocalDateTime guessedAt = LocalDateTime.now();

    // Getters & Setters
    public String getGuildId() { return guildId; }
    public void setGuildId(String guildId) { this.guildId = guildId; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public Integer getRoundNumber() { return roundNumber; }
    public void setRoundNumber(Integer roundNumber) { this.roundNumber = roundNumber; }

    public LocalDateTime getGuessedAt() { return guessedAt; }
    public void setGuessedAt(LocalDateTime guessedAt) { this.guessedAt = guessedAt; }
}
