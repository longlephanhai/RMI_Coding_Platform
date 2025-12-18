package com.rmi.coding.platform.service;

import com.rmi.coding.platform.common.ScoreboardCallback;
import com.rmi.coding.platform.model.Scoreboard;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ScoreboardService extends Remote {
    List<Scoreboard> getScoreboard(int contestId) throws RemoteException;
    void registerCallback(int contestId, ScoreboardCallback callback) throws RemoteException;
    void unregisterCallback(int contestId, ScoreboardCallback callback) throws RemoteException;
    void notifyScoreboardChanged(int contestId) throws RemoteException;
}
