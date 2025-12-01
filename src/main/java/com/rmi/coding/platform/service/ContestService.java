package com.rmi.coding.platform.service;

import com.rmi.coding.platform.model.Contest;
import com.rmi.coding.platform.model.Problem;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ContestService extends Remote {
    boolean createContest(Contest contest) throws RemoteException;
    List<Contest> getContests() throws RemoteException;
    boolean deleteContest(int contestId) throws RemoteException;

    boolean addProblem(Problem problem) throws RemoteException;
    List<Problem> getProblems() throws RemoteException;
}
