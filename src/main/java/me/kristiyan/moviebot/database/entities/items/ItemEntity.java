package me.kristiyan.moviebot.database.entities.items;

import jakarta.persistence.*;
import me.kristiyan.moviebot.enums.RarityEnum;

import java.util.UUID;

@Entity
@Table(name = "items", indexes = {
        @Index(name = "idx_item", columnList = "id")
})
public class ItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name",  nullable = false)
    private String name;

    @Column(name = "rarity", nullable = false)
    private RarityEnum rarity;

    @Column(name = "description", nullable = false)
    private String description;

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public RarityEnum getRarity() {
        return rarity;
    }
    public void setRarity(RarityEnum rarity) {
        this.rarity = rarity;
    }

}
