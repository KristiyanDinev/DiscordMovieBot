package me.kristiyan.moviebot.database.entities.movie;

import jakarta.persistence.*;

@Entity
@Table(name = "movie_categories")
public class MovieCategoryEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private MovieEntity movie;

    @Column(name = "category", length = 30, nullable = false)
    private String category;

    // required for JPA composite key classes
    public MovieCategoryEntity() {}

    public MovieCategoryEntity(MovieEntity movie, String category) {
        this.movie = movie;
        this.category = category;
    }

    // Getters & Setters
    public MovieEntity getMovie() { return movie; }
    public void setMovie(MovieEntity movie) { this.movie = movie; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}