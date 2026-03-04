package me.kristiyan.moviebot.database.entities.activity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bot_usage")
public class BotUsageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String guildId;

    @Column(nullable = false)
    private String guildName;

    @Column(nullable = false)
    private String memberId;

    @Column(nullable = false)
    private String memberNickname;

    @Column(nullable = false)
    private String command;

    @Column(name = "used_at")
    private LocalDateTime guessedAt = LocalDateTime.now();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getGuildId() {
        return guildId;
    }
    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public String getGuildName() {
        return guildName;
    }
    public void setGuildName(String guildName) {
        this.guildName = guildName;
    }

    public String getMemberId() {
        return memberId;
    }
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberNickname() {
        return memberNickname;
    }
    public void setMemberNickname(String memberNickname) {
        this.memberNickname = memberNickname;
    }

    public LocalDateTime getGuessedAt() {
        return guessedAt;
    }

    public void setGuessedAt(LocalDateTime guessedAt) {
        this.guessedAt = guessedAt;
    }

    public String getCommand() {
        return command;
    }
    public void setCommand(String command) {
        this.command = command;
    }
}