package com.rmi.coding.platform.service;

import com.rmi.coding.platform.config.DatabaseConnection;
import com.rmi.coding.platform.model.ContestParticipant;
import com.rmi.coding.platform.repository.ContestParticipantRepository;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.util.List;

public class ContestParticipantServiceImpl extends UnicastRemoteObject implements ContestParticipantService {

    private final ContestParticipantRepository contestParticipantRepository;

    public ContestParticipantServiceImpl() throws RemoteException {
        super();
        try {
            Connection connection = DatabaseConnection.getConnection();
            contestParticipantRepository = new ContestParticipantRepository(connection);
        } catch (Exception e) {
            throw new RemoteException("DB Connection Error", e);
        }
    }

    @Override
    public boolean joinContest(int contestId, int userId) throws RemoteException {
        try {
            return contestParticipantRepository.joinContest(contestId, userId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Join contest failed", e);
        }
    }

    @Override
    public boolean isUserJoined(int contestId, int userId) throws RemoteException {
        try {
            return contestParticipantRepository.isUserJoined(contestId, userId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Check user joined contest failed", e);
        }
    }

    @Override
    public List<ContestParticipant> getParticipants(int contestId) throws RemoteException {
        try {
            return contestParticipantRepository.getParticipantsByContest(contestId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Get Participants failed", e);
        }
    }

    @Override
    public int countParticipants(int contestId) throws RemoteException {
        try {
            return contestParticipantRepository.countParticipants(contestId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Count Participant failed", e);
        }
    }
}
