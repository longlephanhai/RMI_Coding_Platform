package com.rmi.coding.platform.model;

import java.time.LocalDateTime;

public class Submission {
    private int id;
    private int userId;
    private int problemId;
    private String language; // "Java", "Python", ...
    private String code;
    private boolean passed;
    private int passedTests;
    private int totalTests;
    private long executionTime; // ms
    private LocalDateTime submittedAt;

    public Submission() {}

    public Submission(int userId, int problemId, String language, String code) {
        this.userId = userId;
        this.problemId = problemId;
        this.language = language;
        this.code = code;
        this.submittedAt = LocalDateTime.now();
    }

    // getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getProblemId() { return problemId; }
    public void setProblemId(int problemId) { this.problemId = problemId; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }

    public int getPassedTests() { return passedTests; }
    public void setPassedTests(int passedTests) { this.passedTests = passedTests; }

    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }

    public long getExecutionTime() { return executionTime; }
    public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
