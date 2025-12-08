package com.rmi.coding.platform.agents.result;

import java.io.Serial;
import java.io.Serializable;

public class ScriptResult implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean passedAll;
    private int passedCount;
    private int totalCount;
    private long executionTime; // ms tổng
    private String rawOutput;

    public ScriptResult() {
    }

    public ScriptResult(boolean passedAll, int passedCount, int totalCount, long executionTime, String rawOutput) {
        this.passedAll = passedAll;
        this.passedCount = passedCount;
        this.totalCount = totalCount;
        this.executionTime = executionTime;
        this.rawOutput = rawOutput;
    }

    public boolean isPassedAll() {
        return passedAll;
    }

    public void setPassedAll(boolean passedAll) {
        this.passedAll = passedAll;
    }

    public int getPassedCount() {
        return passedCount;
    }

    public void setPassedCount(int passedCount) {
        this.passedCount = passedCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public String getRawOutput() {
        return rawOutput;
    }

    public void setRawOutput(String rawOutput) {
        this.rawOutput = rawOutput;
    }
}
