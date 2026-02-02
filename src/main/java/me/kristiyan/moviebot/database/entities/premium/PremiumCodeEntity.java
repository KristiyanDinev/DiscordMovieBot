package me.kristiyan.moviebot.database.entities.premium;

import jakarta.persistence.*;

@Entity
@Table(name = "premium_code", indexes = {
    @Index(name = "idx_premium_code", columnList = "code")
})
public class PremiumCodeEntity {

    @Id
    @Column(nullable = false, length = 20)
    private String code;

    // Constructors
    public PremiumCodeEntity() {
    }

    public PremiumCodeEntity(String code) {
        this.code = code;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}