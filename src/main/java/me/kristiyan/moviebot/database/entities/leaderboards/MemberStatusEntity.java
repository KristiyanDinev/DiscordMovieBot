package me.kristiyan.moviebot.database.entities.leaderboards;

import jakarta.persistence.*;

@Entity
@Table(name = "member_stats", indexes = {
        @Index(name = "idx_member_leaderboard", columnList = "status_type, correct_guesses DESC, guild_id")
})
public class MemberStatusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "guild_id",  nullable = false)
    private String guildId;

    @Column(name = "guild_name",  nullable = false)
    private String guildName;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "member_nickname", nullable = false)
    private String memberNickname;

    @Column(name = "status_type", length = 30, nullable = false)
    private String statusType;

    @Column(name = "correct_guesses",  nullable = false)
    private int correctGuesses = 0;

    // Getters & Setters
    public String getGuildId() { return guildId; }
    public void setGuildId(String guildId) { this.guildId = guildId; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getMemberNickname() { return memberNickname; }
    public void setMemberNickname(String memberNickname) { this.memberNickname = memberNickname; }

    public String getStatusType() { return statusType; }
    public void setStatusType(String statType) { this.statusType = statType; }

    public int getCorrectGuesses() { return correctGuesses; }
    public void setCorrectGuesses(int correctGuesses) { this.correctGuesses = correctGuesses; }

    public String getGuildName() { return guildName; }
    public void setGuildName(String guildName) { this.guildName = guildName; }
}
