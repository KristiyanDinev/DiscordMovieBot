package me.kristiyan.moviebot.database.entities.games;

import jakarta.persistence.*;
import me.kristiyan.moviebot.database.entities.movie.MovieEntity;
import me.kristiyan.moviebot.enums.GamemodeEnum;

import java.time.LocalDateTime;

@Entity
@Table(name = "active_games", indexes = {
        @Index(name = "idx_active_games_guild", columnList = "guild_id")
})
public class ActiveGameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "guild_id", nullable = false)
    private String guildId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "movie_id", nullable = false)
    private MovieEntity movie;

    @Column(name = "current_round", nullable = false)
    private int currentRound = 1;

    @Column(name = "total_rounds", nullable = false)
    private int totalRounds;

    @Column(name = "high_stakes", nullable = false)
    private boolean highStakes = false;

    @Column(name = "locked", nullable = false)
    private boolean locked = false;

    @Column(name = "locked_member_id") // If NULL, it is the public guild game
    private String lockedMemberId = null;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "gamemode", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private GamemodeEnum gamemode;

    // Getters & Setters
    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public MovieEntity getMovie() {
        return movie;
    }

    public void setMovie(MovieEntity movie) {
        this.movie = movie;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(int totalRounds) {
        this.totalRounds = totalRounds;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public boolean isHighStakes() {
        return highStakes;
    }

    public void setHighStake(boolean highStakes) {
        this.highStakes = highStakes;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getLockedMemberId() {
        return lockedMemberId;
    }

    public void setLockedMemberId(String lockedMemberId) {
        this.lockedMemberId = lockedMemberId;
    }

    public GamemodeEnum getGamemode() {
        return gamemode;
    }
    public void setGamemode(GamemodeEnum gamemode) {
        this.gamemode = gamemode;
    }
}
