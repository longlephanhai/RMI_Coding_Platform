package com.rmi.coding.platform.model;

public class LeaderboardEntry {
    private int userId;
    private String username;
    private int score; // tổng số test case pass

    // getters & setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}
