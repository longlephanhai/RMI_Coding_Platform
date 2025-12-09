package com.rmi.coding.platform.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class Submission implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private int id;
    private Integer contestId;
    private int userId;
    private int problemId;
    private String language;
    private String code;
    private boolean passed;
    private int passedTests;
    private int totalTests;
    private long executionTime;
    private LocalDateTime submittedAt;

    public Submission() {
    }

    // Practice mode (không có contest)
    public Submission(int userId, int problemId, String language, String code) {
        this.userId = userId;
        this.problemId = problemId;
        this.language = language;
        this.code = code;
        this.submittedAt = LocalDateTime.now();
        this.contestId = null;
    }

    // Contest mode (có contest)
    public Submission(Integer contestId, int userId, int problemId, String language, String code) {
        this.contestId = contestId;
        this.userId = userId;
        this.problemId = problemId;
        this.language = language;
        this.code = code;
        this.submittedAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getContestId() {
        return contestId;
    }

    public void setContestId(Integer contestId) {
        this.contestId = contestId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProblemId() {
        return problemId;
    }

    public void setProblemId(int problemId) {
        this.problemId = problemId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public int getPassedTests() {
        return passedTests;
    }

    public void setPassedTests(int passedTests) {
        this.passedTests = passedTests;
    }

    public int getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(int totalTests) {
        this.totalTests = totalTests;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
