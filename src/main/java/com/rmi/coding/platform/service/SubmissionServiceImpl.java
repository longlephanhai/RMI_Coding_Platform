package com.rmi.coding.platform.service;

import com.rmi.coding.platform.config.DatabaseConnection;
import com.rmi.coding.platform.model.Submission;
import com.rmi.coding.platform.repository.SubmissionRepository;
import com.rmi.coding.platform.agents.result.ScriptResult;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.util.List;

public class SubmissionServiceImpl extends UnicastRemoteObject implements SubmissionService {

    private final SubmissionRepository submissionRepository;

    public SubmissionServiceImpl() throws RemoteException {
        super();
        try {
            Connection conn = DatabaseConnection.getConnection();
            submissionRepository = new SubmissionRepository(conn);
        } catch (Exception e) {
            throw new RemoteException("DB Connection error", e);
        }
    }

    @Override
    public Submission submit(int userId, int problemId, String language, String code) throws RemoteException {
        try {
            Submission submission = new Submission(userId, problemId, language, code);
            return submissionRepository.save(submission);
        } catch (Exception e) {
            throw new RemoteException("Error submitting practice solution", e);
        }
    }

    @Override
    public Submission submitInContest(int contestId, int userId, int problemId, String language, String code)
            throws RemoteException {
        try {
            Submission submission = new Submission(contestId, userId, problemId, language, code);
            return submissionRepository.save(submission);
        } catch (Exception e) {
            throw new RemoteException("Error submitting contest solution", e);
        }
    }

    @Override
    public void submitWithResult(Integer contestId, int userId, int problemId, String language, String code, ScriptResult result) throws RemoteException {
        try {
            Submission s = new Submission(contestId, userId, problemId, language, code);
            s.setPassed(result.isPassedAll());
            s.setPassedTests(result.getPassedCount());
            s.setTotalTests(result.getTotalCount());
            s.setExecutionTime(result.getExecutionTime());
            submissionRepository.save(s);
        } catch (Exception e) {
            throw new RemoteException("Failed to save submission with result", e);
        }
    }

    @Override
    public List<Submission> getSubmissionsByUser(int userId) throws RemoteException {
        try {
            return submissionRepository.findByUserId(userId);
        } catch (Exception e) {
            throw new RemoteException("Error getting submissions by user", e);
        }
    }

    @Override
    public List<Submission> getSubmissionsByProblem(int problemId) throws RemoteException {
        try {
            return submissionRepository.findByProblemId(problemId);
        } catch (Exception e) {
            throw new RemoteException("Error getting submissions by problem", e);
        }
    }

    @Override
    public List<Submission> getSubmissionsByUserAndProblem(int userId, int problemId) throws RemoteException {
        try {
            return submissionRepository.findByUserIdAndProblemId(userId, problemId);
        } catch (Exception e) {
            throw new RemoteException("Error getting submissions by user and problem", e);
        }
    }

    @Override
    public List<Submission> getSubmissionsByContest(int contestId) throws RemoteException {
        try {
            return submissionRepository.findByContestId(contestId);
        } catch (Exception e) {
            throw new RemoteException("Error getting submissions by contest", e);
        }
    }

    @Override
    public Submission getLatestSubmissionInContest(int userId, int contestId) throws RemoteException {
        try {
            return submissionRepository.findLatestInContest(userId, contestId);
        } catch (Exception e) {
            throw new RemoteException("Error getting latest submission in contest", e);
        }
    }

    @Override
    public Submission getSubmissionById(int id) throws RemoteException {
        try {
            return submissionRepository.findById(id);
        } catch (Exception e) {
            throw new RemoteException("Error getting submission by id", e);
        }
    }
}
