package me.kristiyan.moviebot.database.entities.profiles;

import jakarta.persistence.*;
import me.kristiyan.moviebot.database.entities.items.ItemEntity;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "profile_items")
public class ProfileItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private ProfileEntity profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemEntity item;

    @Column(name = "acquired_at", nullable = false)
    private LocalDateTime acquiredAt;

    // Default constructor for JPA
    public ProfileItemEntity() {}

    public ProfileItemEntity(ProfileEntity profile, ItemEntity item) {
        this.profile = profile;
        this.item = item;
        this.acquiredAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }

    public ProfileEntity getProfile() { return profile; }
    public void setProfile(ProfileEntity profile) { this.profile = profile; }

    public ItemEntity getItem() { return item; }
    public void setItem(ItemEntity item) { this.item = item; }

    public LocalDateTime getAcquiredAt() { return acquiredAt; }
    public void setAcquiredAt(LocalDateTime acquiredAt) { this.acquiredAt = acquiredAt; }
}