package com.rmi.coding.platform.service;

import com.rmi.coding.platform.config.DatabaseConnection;
import com.rmi.coding.platform.model.Contest;
import com.rmi.coding.platform.model.Problem;
import com.rmi.coding.platform.repository.ContestRepository;
import com.rmi.coding.platform.repository.ProblemRepository;

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
    public void createContest(Contest contest) throws RemoteException {
        try {
            int id = contestRepository.createContest(contest);
        } catch (Exception e) {
            e.printStackTrace();
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
    public List<Problem> getProblems(int contestId) throws RemoteException {
        try {
            return contestRepository.listProblemsByContestId(contestId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Boolean checkStatusContest(int contestId) throws Exception {
        try {
            return contestRepository.checkStatusContest(contestId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
