package com.rmi.coding.platform.service;

import com.rmi.coding.platform.model.TestCase;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface TestCaseService extends Remote {

    List<TestCase> getTestCasesByProblemId(int problemId) throws RemoteException;

    int createTestCase(TestCase testCase) throws RemoteException;
}
