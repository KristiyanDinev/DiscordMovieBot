package me.kristiyan.moviebot.database.entities.leaderboards;

import jakarta.persistence.*;

@Entity
@Table(name = "guild_members",
        indexes = {
                @Index(name = "idx_guild_member", columnList = "guild_id, member_id"),
                @Index(name = "idx_member_id", columnList = "member_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_guild_member", columnNames = {"guild_id", "member_id"})
        }
)
public class GuildMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guild_id", nullable = false)
    private GuildEntity guild;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "member_nickname", nullable = false)
    private String memberNickname;

    @Column(name = "xp", nullable = false)
    private long xp;

    // Getters & Setters
    public Long getId() { return id; }

    public GuildEntity getGuild() { return guild; }
    public void setGuild(GuildEntity guild) { this.guild = guild; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getMemberNickname() { return memberNickname; }
    public void setMemberNickname(String memberNickname) { this.memberNickname = memberNickname; }

    public long getXp() { return xp; }
    public void setXp(long xp) { this.xp = xp; }

}