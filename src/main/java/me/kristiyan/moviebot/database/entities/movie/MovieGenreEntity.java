package me.kristiyan.moviebot.database.entities.movie;

import jakarta.persistence.*;

@Entity
@Table(name = "movie_genres")
public class MovieGenreEntity {

    @EmbeddedId
    private MovieGenreKey id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("movieId")
    @JoinColumn(name = "movie_id", nullable = false)
    private MovieEntity movie;

    // required for JPA composite key classes
    public MovieGenreEntity() {}

    public MovieGenreEntity(MovieEntity movie, String genre) {
        this.movie = movie;
        // Set the value inside the ID object instead
        this.id = new MovieGenreKey(movie.getId(), genre);
    }

    // Update your getter/setter to use the ID object
    public String getGenre() {
        return id != null ? id.getGenre() : null;
    }

    public void setGenre(String genre) {
        if (this.id == null) this.id = new MovieGenreKey();
        this.id.setGenre(genre);
    }
}