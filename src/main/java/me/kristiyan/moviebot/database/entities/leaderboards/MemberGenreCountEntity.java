package me.kristiyan.moviebot.database.entities.leaderboards;

import jakarta.persistence.*;

@Entity
@Table(name = "member_genre_count",
        indexes = {
                @Index(name = "idx_member_genre_member", columnList = "member_id"),
                @Index(name = "idx_member_genre_genre", columnList = "genre")
        }
)
public class MemberGenreCountEntity {

    @EmbeddedId
    private MemberGenreKey id;

    @Column(name = "count", nullable = false)
    private int count = 0;

    public MemberGenreCountEntity() {}

    public MemberGenreCountEntity(String memberId, String genre, int count) {
        this.id = new MemberGenreKey(memberId, genre);
        this.count = count;
    }

    public String getMemberId() { return id == null ? null : id.getMemberId(); }
    public String getGenre() { return id == null ? null : id.getGenre(); }

    public MemberGenreKey getId() { return id; }
    public void setId(MemberGenreKey id) { this.id = id; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public void increment(int incrementBy) { this.count += incrementBy; }
}
