package me.kristiyan.moviebot.database.entities.movie;

import java.io.Serializable;
import java.util.Objects;

public class MovieCategoryKey implements Serializable {
    private int movieId; // Must match the name of the field in Entity
    private String category;

    public MovieCategoryKey() {}
    public MovieCategoryKey(int movieId, String category) {
        this.movieId = movieId;
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieCategoryKey that = (MovieCategoryKey) o;
        return movieId == that.movieId && Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, category);
    }
}