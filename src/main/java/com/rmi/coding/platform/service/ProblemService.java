package com.rmi.coding.platform.service;

import com.rmi.coding.platform.model.Problem;
import com.rmi.coding.platform.model.TestCase;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ProblemService extends Remote {
    int createProblem(Problem problem) throws RemoteException;        // Tạo problem, trả id
    int addTestCase(int problemId, TestCase testCase) throws RemoteException; // Thêm test case
    List<Problem> listAllProblems() throws RemoteException;           // Lấy tất cả problem
    List<TestCase> getTestCases(int problemId) throws RemoteException; // Lấy test case của problem
}
