package me.kristiyan.moviebot.database.entities.movie;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies", indexes = {
        @Index(name = "idx_movies_content_type_year_rating", columnList = "content_type, movie_year, rating"),
})
public class MovieEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(name = "gif_url", nullable = false)
    private String gifUrl;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "movie_year", nullable = false)
    private int year;

    // Rating: precision 4, scale 2 allows up to 99.99 (e.g., 8.38)
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal rating;

    @Column(nullable = false)
    private int duration;

    // Popularity: precision 12, scale 4 allows huge numbers (e.g., 23.3178 or 1500.50)
    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal popularity;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "hint", nullable = false)
    private String hint;

    @Column(name = "emoji", nullable = false)
    private String emoji;

    @Column(name = "quote", nullable = false)
    private String quote;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieGenreEntity> genres = new ArrayList<>();

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGifUrl() { return gifUrl; }
    public void setGifUrl(String gifUrl) { this.gifUrl = gifUrl; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<MovieGenreEntity> getGenres() { return genres; }
    public void setGenres(List<MovieGenreEntity> genres) { this.genres = genres; }

    public void addGenre(String genreName) {
        this.genres.add(new MovieGenreEntity(this, genreName));
    }

    public BigDecimal getPopularity() { return popularity; }
    public void setPopularity(BigDecimal popularity) { this.popularity = popularity; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public String getQuote() { return quote; }
    public void setQuote(String quote) { this.quote = quote; }
}
