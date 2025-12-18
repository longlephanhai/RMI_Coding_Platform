package com.rmi.coding.platform.service;

import com.rmi.coding.platform.model.ContestParticipant;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ContestParticipantService extends Remote {
    boolean joinContest(int contestId, int userId) throws RemoteException;

    boolean isUserJoined(int contestId, int userId) throws RemoteException;

    List<ContestParticipant> getParticipants(int contestId) throws RemoteException;

    int countParticipants(int contestId) throws RemoteException;
}
