package com.rmi.coding.platform.service;

import com.rmi.coding.platform.config.DatabaseConnection;
import com.rmi.coding.platform.model.Contest;
import com.rmi.coding.platform.model.Problem;
import com.rmi.coding.platform.repository.ContestRepository;
import com.rmi.coding.platform.repository.ProblemRepository;
import com.rmi.coding.platform.repository.TestCaseRepository;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.util.List;

public class ContestServiceImpl extends UnicastRemoteObject implements ContestService {

    private final ContestRepository contestRepository;
    private final ProblemRepository problemRepository;

    public ContestServiceImpl() throws RemoteException {
        super();
        try {
            Connection conn = DatabaseConnection.getConnection();
            contestRepository = new ContestRepository(conn);
            problemRepository = new ProblemRepository(conn);
        } catch (Exception e) {
            throw new RemoteException("DB Connection error", e);
        }
    }

    @Override
    public boolean createContest(Contest contest) throws RemoteException {
        try {
            int id = contestRepository.createContest(contest);
            return id > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Contest> getContests() throws RemoteException {
        try {
            return contestRepository.listAllContests();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deleteContest(int contestId) throws RemoteException {
        try {
            return contestRepository.deleteContest(contestId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addProblem(Problem problem) throws RemoteException {
        try {
            problemRepository.createProblem(problem);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Problem> getProblems() throws RemoteException {
        try {
            return problemRepository.listAllProblems();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
