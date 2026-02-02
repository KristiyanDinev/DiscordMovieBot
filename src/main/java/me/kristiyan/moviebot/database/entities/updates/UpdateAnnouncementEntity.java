package me.kristiyan.moviebot.database.entities.updates;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "updates")
public class UpdateAnnouncementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(name = "update_released_at", nullable = false)
    private LocalDate releasedAt = LocalDate.now();

    @Column(nullable = false)
    private String title;

    @Column(name = "short_description", nullable = false, length = 1000)
    private String shortDescription;

    // Getters & Setters
    public Short getId() { return id; }
    public void setId(Short id) { this.id = id; }

    public LocalDate getReleasedAt() { return releasedAt; }
    public void setReleasedAt(LocalDate releasedAt) { this.releasedAt = releasedAt; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
}
