package me.kristiyan.moviebot.database.entities.premium;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "premium_users", indexes = {
        @Index(name = "idx_premium_users_user_id", columnList = "user_id"),
})
public class PremiumUserEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "activated_at", nullable = false)
    private LocalDateTime activatedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        if (activatedAt == null) {
            activatedAt = LocalDateTime.now();
        }
    }

    // Constructors
    public PremiumUserEntity() {
    }

    public PremiumUserEntity(String userId, LocalDateTime expiresAt) {
        this.userId = userId;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(LocalDateTime activatedAt) {
        this.activatedAt = activatedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }


    public boolean isActive() {
        return expiresAt.isAfter(LocalDateTime.now());
    }
}
