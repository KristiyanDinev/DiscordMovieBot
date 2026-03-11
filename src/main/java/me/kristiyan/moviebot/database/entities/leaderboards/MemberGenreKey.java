package me.kristiyan.moviebot.database.entities.leaderboards;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class MemberGenreKey implements Serializable {
    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "genre", nullable = false)
    private String genre;

    public MemberGenreKey() {}

    public MemberGenreKey(String memberId, String genre) {
        this.memberId = memberId;
        this.genre = genre;
    }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberGenreKey key = (MemberGenreKey) o;
        return memberId.equals(key.memberId) && genre.equals(key.genre);
    }

    @Override
    public int hashCode() {
        return (memberId + "::" + genre).hashCode();
    }
}
