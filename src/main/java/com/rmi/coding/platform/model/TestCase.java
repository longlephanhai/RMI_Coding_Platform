package com.rmi.coding.platform.model;

import java.io.Serial;
import java.io.Serializable;

public class TestCase implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private int id;
    private int problemId;
    private String input;
    private String expectedOutput;

    public TestCase() {}

    public TestCase(int id, int problemId, String input, String expectedOutput) {
        this.id = id;
        this.problemId = problemId;
        this.input = input;
        this.expectedOutput = expectedOutput;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProblemId() { return problemId; }
    public void setProblemId(int problemId) { this.problemId = problemId; }

    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }

    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
}
