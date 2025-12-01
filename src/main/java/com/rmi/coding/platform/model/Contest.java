package com.rmi.coding.platform.model;

import java.time.LocalDateTime;
import java.util.List;

public class Contest {
    private int id;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<Integer> problemIds;

    public Contest() {}

    // getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public List<Integer> getProblemIds() { return problemIds; }
    public void setProblemIds(List<Integer> problemIds) { this.problemIds = problemIds; }
}
