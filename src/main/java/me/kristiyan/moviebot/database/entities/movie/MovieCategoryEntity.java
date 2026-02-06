package me.kristiyan.moviebot.database.entities.movie;

import jakarta.persistence.*;

@Entity
@Table(name = "movie_categories")
public class MovieCategoryEntity {

    @EmbeddedId
    private MovieCategoryKey id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("movieId")
    @JoinColumn(name = "movie_id", nullable = false)
    private MovieEntity movie;

    // required for JPA composite key classes
    public MovieCategoryEntity() {}

    public MovieCategoryEntity(MovieEntity movie, String category) {
        this.movie = movie;
        // Set the value inside the ID object instead
        this.id = new MovieCategoryKey(movie.getId(), category);
    }

    // Update your getter/setter to use the ID object
    public String getCategory() {
        return id != null ? id.getCategory() : null;
    }

    public void setCategory(String category) {
        if (this.id == null) this.id = new MovieCategoryKey();
        this.id.setCategory(category);
    }
}