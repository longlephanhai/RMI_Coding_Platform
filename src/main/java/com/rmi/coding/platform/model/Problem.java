package com.rmi.coding.platform.model;

import java.time.LocalDateTime;
import java.util.List;

public class Problem {
    private int id;
    private String title;
    private String description;
    private String difficulty; // "Easy", "Medium", "Hard"
    private String starterCode;
    private List<TestCase> testCases;
    private LocalDateTime createdAt;

    public Problem() {}

    public Problem(int id, String title, String description, String difficulty, String starterCode, List<TestCase> testCases) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.starterCode = starterCode;
        this.testCases = testCases;
        this.createdAt = LocalDateTime.now();
    }

    // getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getStarterCode() { return starterCode; }
    public void setStarterCode(String starterCode) { this.starterCode = starterCode; }

    public List<TestCase> getTestCases() { return testCases; }
    public void setTestCases(List<TestCase> testCases) { this.testCases = testCases; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
