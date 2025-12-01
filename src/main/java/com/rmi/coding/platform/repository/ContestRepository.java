package com.rmi.coding.platform.repository;

import com.rmi.coding.platform.model.Contest;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ContestRepository {
    private final Connection connection;

    public ContestRepository(Connection connection) {
        this.connection = connection;
    }


    public int createContest(Contest contest) throws SQLException {
        String sql = "INSERT INTO contests (title, start_time, end_time) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, contest.getTitle());
            stmt.setTimestamp(2, Timestamp.valueOf(contest.getStartTime()));
            stmt.setTimestamp(3, Timestamp.valueOf(contest.getEndTime()));
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int contestId = rs.getInt(1);
                addProblemsToContest(contestId, contest.getProblemIds());
                return contestId;
            }
        }
        return -1;
    }

    // Hàm thêm problem vào contest
    public void addProblemsToContest(int contestId, List<Integer> problemIds) throws SQLException {
        if (problemIds == null || problemIds.isEmpty()) return;
        String sql = "INSERT INTO contest_problems (contest_id, problem_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Integer pid : problemIds) {
                stmt.setInt(1, contestId);
                stmt.setInt(2, pid);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public List<Contest> listAllContests() throws SQLException {
        List<Contest> contests = new ArrayList<>();
        String sql = "SELECT * FROM contests";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Contest c = new Contest();
                c.setId(rs.getInt("id"));
                c.setTitle(rs.getString("title"));
                c.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                c.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
                contests.add(c);
            }
        }
        return contests;
    }


    public boolean deleteContest(int contestId) throws SQLException {
        String sql = "DELETE FROM contests WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contestId);
            return stmt.executeUpdate() > 0;
        }
    }

}
