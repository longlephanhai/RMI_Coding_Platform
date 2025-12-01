package com.rmi.coding.platform.repository;

import com.rmi.coding.platform.model.Problem;
import com.rmi.coding.platform.model.TestCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProblemRepository {
    private final Connection connection;
    private final ObjectMapper mapper = new ObjectMapper();

    public ProblemRepository(Connection connection) {
        this.connection = connection;
    }

    public int createProblem(Problem problem) throws Exception {
        String sql = "INSERT INTO problems (title, description, difficulty, starter_code, created_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, problem.getTitle());
            stmt.setString(2, problem.getDescription());
            stmt.setString(3, problem.getDifficulty());

            String starterJson = mapper.writeValueAsString(problem.getStarterCode());
            stmt.setString(4, starterJson);

            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int problemId = rs.getInt(1);

                if (problem.getTestCases() != null) {
                    TestCaseRepository testCaseRepo = new TestCaseRepository(connection);
                    for (TestCase tc : problem.getTestCases()) {
                        tc.setProblemId(problemId);
                        testCaseRepo.createTestCase(tc);
                    }
                }
                return problemId;
            }
        }
        return -1;
    }

    public Problem getProblemById(int id) throws Exception {
        String sql = "SELECT * FROM problems WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Problem problem = new Problem();
                problem.setId(rs.getInt("id"));
                problem.setTitle(rs.getString("title"));
                problem.setDescription(rs.getString("description"));
                problem.setDifficulty(rs.getString("difficulty"));

                // Chuyển JSON string thành Map
                String starterJson = rs.getString("starter_code");
                Map<String, String> starterMap = mapper.readValue(starterJson, Map.class);
                problem.setStarterCode(starterMap);

                problem.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                TestCaseRepository testCaseRepo = new TestCaseRepository(connection);
                problem.setTestCases(testCaseRepo.getTestCasesByProblemId(id));

                return problem;
            }
        }
        return null;
    }

    public List<Problem> listAllProblems() throws Exception {
        List<Problem> problems = new ArrayList<>();
        String sql = "SELECT * FROM problems";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                problems.add(getProblemById(rs.getInt("id")));
            }
        }
        return problems;
    }
}
