package com.rmi.coding.platform.service;

import com.rmi.coding.platform.config.DatabaseConnection;
import com.rmi.coding.platform.model.TestCase;
import com.rmi.coding.platform.repository.ProblemRepository;
import com.rmi.coding.platform.repository.TestCaseRepository;
import com.rmi.coding.platform.service.TestCaseService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class TestCaseServiceImpl extends UnicastRemoteObject implements TestCaseService {

    private final TestCaseRepository testCaseRepo;

    public TestCaseServiceImpl() throws RemoteException {
        super();
        try {
            Connection conn = DatabaseConnection.getConnection();
            testCaseRepo = new TestCaseRepository(conn);
        } catch (Exception e) {
            throw new RemoteException("DB Connection error", e);
        }
    }

    @Override
    public List<TestCase> getTestCasesByProblemId(int problemId) throws RemoteException {
        try {
            return testCaseRepo.getTestCasesByProblemId(problemId);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Cannot fetch test cases: " + e.getMessage());
        }
    }

    @Override
    public int createTestCase(TestCase testCase) throws RemoteException {
        try {
            return testCaseRepo.createTestCase(testCase);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Cannot create test case: " + e.getMessage());
        }
    }
}
