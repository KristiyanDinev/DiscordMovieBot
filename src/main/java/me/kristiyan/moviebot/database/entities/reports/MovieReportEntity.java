package me.kristiyan.moviebot.database.entities.reports;

import jakarta.persistence.*;
import me.kristiyan.moviebot.database.entities.movie.MovieEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "movie_reports", uniqueConstraints = {
        @UniqueConstraint(name = "uc_member_movie_report", columnNames = {"member_id", "movie_id"})
})
public class MovieReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private MovieEntity movie;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(name = "reported_at")
    private LocalDateTime reportedAt = LocalDateTime.now();

    // Getters & Setters
    public long getId() { return id; }
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public MovieEntity getMovie() { return movie; }
    public void setMovie(MovieEntity movie) { this.movie = movie; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
