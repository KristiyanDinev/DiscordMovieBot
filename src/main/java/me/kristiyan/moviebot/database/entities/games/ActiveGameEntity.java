package me.kristiyan.moviebot.database.entities.games;

import jakarta.persistence.*;
import me.kristiyan.moviebot.database.entities.movie.MovieEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "active_games", indexes = {
        @Index(name = "idx_active_games_guild", columnList = "guild_id")
})
public class ActiveGameEntity {

    @Id
    @Column(name = "guild_id")
    private String guildId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private MovieEntity movie;

    @Column(name = "current_round")
    private int currentRound = 1;

    @Column(name = "total_rounds", nullable = false)
    private int totalRounds;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    // Getters & Setters
    public String getGuildId() { return guildId; }
    public void setGuildId(String guildId) { this.guildId = guildId; }

    public MovieEntity getMovie() { return movie; }
    public void setMovie(MovieEntity movie) { this.movie = movie; }

    public int getCurrentRound() { return currentRound; }
    public void setCurrentRound(int currentRound) { this.currentRound = currentRound; }

    public int getTotalRounds() { return totalRounds; }
    public void setTotalRounds(int totalRounds) { this.totalRounds = totalRounds; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
}
