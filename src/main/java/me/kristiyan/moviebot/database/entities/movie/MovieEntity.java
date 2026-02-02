package me.kristiyan.moviebot.database.entities.movie;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal rating;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MovieCategoryEntity> categories = new HashSet<>();

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

    public Set<MovieCategoryEntity> getCategories() { return categories; }
    public void setCategories(Set<MovieCategoryEntity> categories) { this.categories = categories; }

    public void addCategory(String categoryName) {
        this.categories.add(new MovieCategoryEntity(this, categoryName));
    }
}
