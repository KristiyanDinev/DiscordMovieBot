package me.kristiyan.moviebot.database.dto;

public class MemberLeaderboardDTO {
    private String memberId;
    private String nickname;
    private long xp;
    private int level;
    private int totalCorrectGuesses;

    // Getters & Setters
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public long getXp() { return xp; }
    public void setXp(long xp) { this.xp = xp; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getTotalCorrectGuesses() { return totalCorrectGuesses; }
    public void setTotalCorrectGuesses(int totalCorrectGuesses) {
        this.totalCorrectGuesses = totalCorrectGuesses;
    }

    @Override
    public String toString() {
        return String.format("%s (Level %d, %d XP) - %d correct guesses",
                nickname, level, xp, totalCorrectGuesses);
    }
}