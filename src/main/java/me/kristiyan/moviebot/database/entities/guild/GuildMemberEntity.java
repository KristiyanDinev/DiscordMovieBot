package me.kristiyan.moviebot.database.entities.guild;

import jakarta.persistence.*;

@Entity
@Table(name = "guild_members",
        indexes = {
                @Index(name = "idx_guild_member", columnList = "guild_id, member_id"),
                @Index(name = "idx_member_id",    columnList = "member_id")
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

    /**
     * Links to the global MemberEntity so premium status and identity
     * are shared across all guilds the user belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(name = "member_nickname", nullable = false)
    private String memberNickname;

    @Column(name = "xp", nullable = false)
    private long xp;

    // Getters & Setters
    public Long getId() { return id; }

    public GuildEntity getGuild() { return guild; }
    public void setGuild(GuildEntity guild) { this.guild = guild; }

    public MemberEntity getMember() { return member; }
    public void setMember(MemberEntity member) { this.member = member; }

    public String getMemberNickname() { return memberNickname; }
    public void setMemberNickname(String memberNickname) { this.memberNickname = memberNickname; }

    public long getXp() { return xp; }
    public void setXp(long xp) { this.xp = xp; }

    /**
     * Convenience — a member is premium in this guild context if either
     * they personally have an active subscription, or the guild itself does.
     */
    public boolean isPremium() {
        return member.isPremium() || guild.isPremium();
    }
}