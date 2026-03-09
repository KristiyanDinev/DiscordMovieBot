package me.kristiyan.moviebot.database.entities.leaderboards;

import jakarta.persistence.*;
import me.kristiyan.moviebot.enums.LeaderboardMemberStatus;

import java.time.LocalDate;

@Entity
@Table(name = "new_member_stats",
        indexes = {
                // For leaderboard queries by guild and status
                @Index(name = "idx_leaderboard", columnList = "guild_member_id, status_type, correct_guesses DESC"),

                // For daily/time-based queries
                @Index(name = "idx_daily_stats", columnList = "guessed_at, status_type"),

                // For member-specific queries across all guilds
                @Index(name = "idx_status_date", columnList = "status_type, guessed_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_status_date",
                        columnNames = {"guild_member_id", "status_type", "guessed_at"}
                )
        }
)
public class MemberStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guild_member_id", nullable = false)
    private GuildMemberEntity guildMember;

    @Column(name = "status_type", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private LeaderboardMemberStatus statusType;

    @Column(name = "correct_guesses", nullable = false)
    private int correctGuesses = 0;

    @Column(name = "guessed_at", nullable = false)
    private LocalDate guessedAt;

    // Constructor
    public MemberStatusEntity() {
        this.guessedAt = LocalDate.now();
    }

    // Getters & Setters
    public Long getId() { return id; }

    public GuildMemberEntity getGuildMember() { return guildMember; }
    public void setGuildMember(GuildMemberEntity guildMember) { this.guildMember = guildMember; }

    public LeaderboardMemberStatus getStatusType() { return statusType; }
    public void setStatusType(LeaderboardMemberStatus statusType) { this.statusType = statusType; }

    public int getCorrectGuesses() { return correctGuesses; }
    public void setCorrectGuesses(int correctGuesses) { this.correctGuesses = correctGuesses; }

    public LocalDate getGuessedAt() { return guessedAt; }
    public void setGuessedAt(LocalDate guessedAt) { this.guessedAt = guessedAt; }
}