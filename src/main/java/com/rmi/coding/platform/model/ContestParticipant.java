package com.rmi.coding.platform.model;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;

public class ContestParticipant implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int contestId;
    private int userId;
    private Timestamp joinedAt;

    public ContestParticipant() {
    }

    public ContestParticipant(int contestId, int userId, Timestamp joinedAt) {
        this.contestId = contestId;
        this.userId = userId;
        this.joinedAt = joinedAt;
    }

    public int getContestId() {
        return contestId;
    }

    public void setContestId(int contestId) {
        this.contestId = contestId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Timestamp getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Timestamp joinedAt) {
        this.joinedAt = joinedAt;
    }
}
