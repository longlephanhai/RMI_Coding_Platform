package com.rmi.coding.platform.model;

import java.util.List;

public class Leaderboard {
    private int problemId;
    private List<LeaderboardEntry> entries;

    public int getProblemId() { return problemId; }
    public void setProblemId(int problemId) { this.problemId = problemId; }

    public List<LeaderboardEntry> getEntries() { return entries; }
    public void setEntries(List<LeaderboardEntry> entries) { this.entries = entries; }
}
