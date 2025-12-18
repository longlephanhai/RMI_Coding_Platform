package com.rmi.coding.platform.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

public class Scoreboard implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int userId;
    private String username;
    private Map<Integer, Integer> problemScores; // {problemId, score}
    private int totalScore;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Map<Integer, Integer> getProblemScores() {
        return problemScores;
    }

    public void setProblemScores(Map<Integer, Integer> problemScores) {
        this.problemScores = problemScores;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }
}
