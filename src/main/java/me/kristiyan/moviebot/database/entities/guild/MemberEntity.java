package me.kristiyan.moviebot.database.entities.guild;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "members",
        indexes = {
                @Index(name = "idx_member_id", columnList = "member_id", unique = true)
        }
)
public class MemberEntity {

    @Id
    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "member_name", nullable = false)
    private String memberName;

    @Column(name = "subscribed_at")
    private LocalDate subscribedAt;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    // Getters & Setters
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public LocalDate getSubscribedAt() { return subscribedAt; }
    public void setSubscribedAt(LocalDate subscribedAt) { this.subscribedAt = subscribedAt; }

    public LocalDate getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDate expiresAt) { this.expiresAt = expiresAt; }

    /** Safe null-check: never subscribed = not premium. */
    public boolean isPremium() {
        return expiresAt != null && expiresAt.isAfter(LocalDate.now());
    }
}