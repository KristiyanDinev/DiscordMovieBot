package me.kristiyan.moviebot.database.dto;

public class MemberLeaderboardDTO {
    private String memberId;
    private String nickname;
    private int totalCorrectGuesses;

    // Getters & Setters
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }


    public int getTotalCorrectGuesses() { return totalCorrectGuesses; }
    public void setTotalCorrectGuesses(int totalCorrectGuesses) {
        this.totalCorrectGuesses = totalCorrectGuesses;
    }

    @Override
    public String toString() {
        return String.format("%s - %d correct guesses",
                nickname, totalCorrectGuesses);
    }
}