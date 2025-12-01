package com.rmi.coding.platform.service;

import com.rmi.coding.platform.model.Problem;
import com.rmi.coding.platform.model.TestCase;
import com.rmi.coding.platform.repository.ProblemRepository;
import com.rmi.coding.platform.repository.TestCaseRepository;
import com.rmi.coding.platform.config.DatabaseConnection;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.util.List;

public class ProblemServiceImpl extends UnicastRemoteObject implements ProblemService {

    private final ProblemRepository problemRepo;
    private final TestCaseRepository testCaseRepo;

    public ProblemServiceImpl() throws RemoteException {
        super();
        try {
            Connection conn = DatabaseConnection.getConnection();
            problemRepo = new ProblemRepository(conn);
            testCaseRepo = new TestCaseRepository(conn);
        } catch (Exception e) {
            throw new RemoteException("DB Connection error", e);
        }
    }

    @Override
    public int createProblem(Problem problem) throws RemoteException {
        try {
            return problemRepo.createProblem(problem);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Error creating problem", e);
        }
    }

    @Override
    public int addTestCase(int problemId, TestCase testCase) throws RemoteException {
        try {
            testCase.setProblemId(problemId);
            return testCaseRepo.createTestCase(testCase);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Error adding test case", e);
        }
    }

    @Override
    public List<Problem> listAllProblems() throws RemoteException {
        try {
            return problemRepo.listAllProblems();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Error listing problems", e);
        }
    }

    @Override
    public List<TestCase> getTestCases(int problemId) throws RemoteException {
        try {
            return testCaseRepo.getTestCasesByProblemId(problemId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Error getting test cases", e);
        }
    }
}
