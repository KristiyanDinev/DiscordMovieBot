package me.kristiyan.moviebot.database.entities.profiles;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profiles", indexes = {
        @Index(name = "idx_profile", columnList = "user_id")
})
public class ProfileEntity {
    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "current_level", nullable = false)
    private long currentLevel;

    @Column(name = "total_correct_guesses", nullable = false)
    private long totalCorrectGuesses;

    @Column(name = "total_correct_guesses_high_stakes", nullable = false)
    private long totalCorrectGuessesHighStakes;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProfileItemEntity> inventory = new ArrayList<>();

    public List<ProfileItemEntity> getInventory() {
        return inventory;
    }

    public void setInventory(List<ProfileItemEntity> inventory) {
        this.inventory = inventory;
    }

    public long getCurrentLevel() {
        return currentLevel;
    }
    public void setCurrentLevel(long currentLevel) {
        this.currentLevel = currentLevel;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public long getTotalCorrectGuesses() {
        return totalCorrectGuesses;
    }
    public void setTotalCorrectGuesses(long totalCorrectGuesses) {
        this.totalCorrectGuesses = totalCorrectGuesses;
    }
    public long getTotalCorrectGuessesHighStakes() {
        return totalCorrectGuessesHighStakes;
    }
    public void setTotalCorrectGuessesHighStakes(long totalCorrectGuessesHighStakes) {
        this.totalCorrectGuessesHighStakes = totalCorrectGuessesHighStakes;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
