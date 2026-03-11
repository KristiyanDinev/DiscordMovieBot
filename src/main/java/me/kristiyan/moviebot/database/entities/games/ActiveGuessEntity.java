package me.kristiyan.moviebot.database.entities.games;

import jakarta.persistence.*;
import me.kristiyan.moviebot.database.entities.movie.MovieEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "active_guesses", indexes = {
        @Index(name = "idx_active_guesses_game", columnList = "active_game_id")
})
public class ActiveGuessEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "active_game_id", nullable = false)
    private ActiveGameEntity activeGame;

    @Column(name = "guild_id", nullable = false)
    private String guildId;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false) // Links to MovieEntity.id
    private MovieEntity movie;

    @Column(name = "guessed_at")
    private LocalDateTime guessedAt = LocalDateTime.now();

    @Column(name = "member_nickname", nullable = false)
    private String memberNickname;


    // Getters & Setters
    public String getGuildId() { return guildId; }
    public void setGuildId(String guildId) { this.guildId = guildId; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public Integer getRoundNumber() { return roundNumber; }
    public void setRoundNumber(Integer roundNumber) { this.roundNumber = roundNumber; }

    public LocalDateTime getGuessedAt() { return guessedAt; }
    public void setGuessedAt(LocalDateTime guessedAt) { this.guessedAt = guessedAt; }

    public String getMemberNickname() { return memberNickname; }
    public void setMemberNickname(String memberNickname) { this.memberNickname = memberNickname; }

    public ActiveGameEntity getActiveGame() { return activeGame; }
    public void setActiveGame(ActiveGameEntity activeGame) { this.activeGame = activeGame; }

    public MovieEntity getMovie() { return movie; }
    public void setMovie(MovieEntity movie) { this.movie = movie; }

}
