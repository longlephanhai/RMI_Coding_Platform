package com.rmi.coding.platform.repository;

import com.rmi.coding.platform.model.TestCase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TestCaseRepository {
    private final Connection connection;

    public TestCaseRepository(Connection connection) {
        this.connection = connection;
    }

    public int createTestCase(TestCase testCase) throws SQLException {
        String sql = "INSERT INTO test_cases (problem_id, input, expected_output) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, testCase.getProblemId());
            stmt.setString(2, testCase.getInput());
            stmt.setString(3, testCase.getExpectedOutput());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    public List<TestCase> getTestCasesByProblemId(int problemId) throws SQLException {
        List<TestCase> testCases = new ArrayList<>();
        String sql = "SELECT * FROM test_cases WHERE problem_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, problemId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TestCase tc = new TestCase();
                tc.setId(rs.getInt("id"));
                tc.setProblemId(rs.getInt("problem_id"));
                tc.setInput(rs.getString("input"));
                tc.setExpectedOutput(rs.getString("expected_output"));
                testCases.add(tc);
            }
        }
        return testCases;
    }
}
