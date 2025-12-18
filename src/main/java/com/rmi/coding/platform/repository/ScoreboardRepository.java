package com.rmi.coding.platform.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreboardRepository {
    private final Connection connection;

    public ScoreboardRepository(Connection connection) {
        this.connection = connection;
    }

    public List<Map<String, Object>> fetchRawScoreboard(int contestId) {
        String sql = """
                    SELECT s.user_id, u.username, s.problem_id,
                           MAX(s.passed_tests) AS best_score
                    FROM submissions s
                    JOIN users u ON s.user_id = u.id
                    WHERE s.contest_id = ?
                    GROUP BY s.user_id, u.username, s.problem_id
                """;

        List<Map<String, Object>> rows = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, contestId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("userId", rs.getInt("user_id"));
                row.put("username", rs.getString("username"));
                row.put("problemId", rs.getInt("problem_id"));
                row.put("score", rs.getInt("best_score"));
                rows.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rows;
    }
}
