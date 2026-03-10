package me.kristiyan.moviebot.database.entities.movie;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MovieGenreKey implements Serializable {
    private int movieId; // Must match the name of the field in Entity
    private String genre;

    public MovieGenreKey() {}
    public MovieGenreKey(int movieId, String genre) {
        this.movieId = movieId;
        this.genre = genre;
    }

    // Getters are required for JPA to access the fields
    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieGenreKey that = (MovieGenreKey) o;
        return movieId == that.movieId && Objects.equals(genre, that.genre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, genre);
    }
}