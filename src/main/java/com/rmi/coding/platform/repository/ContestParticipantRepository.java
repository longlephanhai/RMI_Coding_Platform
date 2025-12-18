package com.rmi.coding.platform.repository;

import com.rmi.coding.platform.model.ContestParticipant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContestParticipantRepository {

    private final Connection connection;

    public ContestParticipantRepository(Connection connection) {
        this.connection = connection;
    }

    public boolean joinContest(int contestId, int userId) throws SQLException {
        String sql = """
                    INSERT IGNORE INTO contest_participants (contest_id, user_id)
                    VALUES (?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contestId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }


    public boolean isUserJoined(int contestId, int userId) throws SQLException {
        String sql = """
                    SELECT 1 FROM contest_participants
                    WHERE contest_id = ? AND user_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contestId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public List<ContestParticipant> getParticipantsByContest(int contestId) throws SQLException {
        List<ContestParticipant> list = new ArrayList<>();

        String sql = """
                    SELECT contest_id, user_id, joined_at
                    FROM contest_participants
                    WHERE contest_id = ?
                    ORDER BY joined_at ASC
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contestId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ContestParticipant cp = new ContestParticipant();
                cp.setContestId(rs.getInt("contest_id"));
                cp.setUserId(rs.getInt("user_id"));
                cp.setJoinedAt(rs.getTimestamp("joined_at"));
                list.add(cp);
            }
        }
        return list;
    }

    public int countParticipants(int contestId) throws SQLException {
        String sql = """
                    SELECT COUNT(*) FROM contest_participants WHERE contest_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contestId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
