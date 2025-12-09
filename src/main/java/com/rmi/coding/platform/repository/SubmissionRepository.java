package com.rmi.coding.platform.repository;

import com.rmi.coding.platform.model.Submission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubmissionRepository {

    private final Connection connection;

    public SubmissionRepository(Connection connection) {
        this.connection = connection;
    }

    public Submission save(Submission submission) throws SQLException {
        String sql = """
                     INSERT INTO submissions\s
                     (contest_id, user_id, problem_id, language, code, passed, passed_tests, total_tests, execution_time, submitted_at)
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                \s""";

        PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        if (submission.getContestId() == null) {
            stmt.setNull(1, Types.INTEGER);
        } else {
            stmt.setInt(1, submission.getContestId());
        }

        stmt.setInt(2, submission.getUserId());
        stmt.setInt(3, submission.getProblemId());
        stmt.setString(4, submission.getLanguage());
        stmt.setString(5, submission.getCode());
        stmt.setBoolean(6, submission.isPassed());
        stmt.setInt(7, submission.getPassedTests());
        stmt.setInt(8, submission.getTotalTests());
        stmt.setLong(9, submission.getExecutionTime());
        stmt.setTimestamp(10, Timestamp.valueOf(submission.getSubmittedAt()));

        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            submission.setId(rs.getInt(1));
        }

        return submission;
    }

    public Submission findById(int id) throws SQLException {
        String sql = "SELECT * FROM submissions WHERE id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, id);

        ResultSet rs = stmt.executeQuery();
        return rs.next() ? mapSubmission(rs) : null;
    }

    public List<Submission> findAll() throws SQLException {
        List<Submission> list = new ArrayList<>();
        String sql = "SELECT * FROM submissions ORDER BY submitted_at DESC";

        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            list.add(mapSubmission(rs));
        }

        return list;
    }

    public List<Submission> findByUserId(int userId) throws SQLException {
        List<Submission> list = new ArrayList<>();
        String sql = "SELECT * FROM submissions WHERE user_id = ? ORDER BY submitted_at DESC";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, userId);

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            list.add(mapSubmission(rs));
        }
        return list;
    }

    public List<Submission> findByProblemId(int problemId) throws SQLException {
        List<Submission> list = new ArrayList<>();
        String sql = "SELECT * FROM submissions WHERE problem_id = ? ORDER BY submitted_at DESC";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, problemId);

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            list.add(mapSubmission(rs));
        }
        return list;
    }

    public List<Submission> findByUserIdAndProblemId(int userId, int problemId) throws SQLException {
        List<Submission> list = new ArrayList<>();
        String sql = "SELECT * FROM submissions WHERE user_id = ? AND problem_id = ? ORDER BY submitted_at DESC";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1,userId);
        stmt.setInt(2,problemId);
        ResultSet rs= stmt.executeQuery();
        while (rs.next()){
            list.add(mapSubmission(rs));
        }
        return list;
    }

    public List<Submission> findByContestId(Integer contestId) throws SQLException {
        List<Submission> list = new ArrayList<>();
        String sql = "SELECT * FROM submissions WHERE contest_id = ? ORDER BY submitted_at ASC";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, contestId);

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            list.add(mapSubmission(rs));
        }
        return list;
    }

    public Submission findLatestInContest(int userId, int contestId) throws SQLException {
        String sql = """
                    SELECT * FROM submissions
                    WHERE user_id = ? AND contest_id = ?
                    ORDER BY submitted_at DESC
                    LIMIT 1
                """;

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, userId);
        stmt.setInt(2, contestId);

        ResultSet rs = stmt.executeQuery();
        return rs.next() ? mapSubmission(rs) : null;
    }

    private Submission mapSubmission(ResultSet rs) throws SQLException {
        Submission s = new Submission();
        s.setId(rs.getInt("id"));

        int contestIdVal = rs.getInt("contest_id");
        s.setContestId(rs.wasNull() ? null : contestIdVal);

        s.setUserId(rs.getInt("user_id"));
        s.setProblemId(rs.getInt("problem_id"));
        s.setLanguage(rs.getString("language"));
        s.setCode(rs.getString("code"));
        s.setPassed(rs.getBoolean("passed"));
        s.setPassedTests(rs.getInt("passed_tests"));
        s.setTotalTests(rs.getInt("total_tests"));
        s.setExecutionTime(rs.getLong("execution_time"));

        Timestamp ts = rs.getTimestamp("submitted_at");
        s.setSubmittedAt(ts.toLocalDateTime());

        return s;
    }
}
