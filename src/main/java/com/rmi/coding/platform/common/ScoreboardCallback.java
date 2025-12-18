package com.rmi.coding.platform.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ScoreboardCallback extends Remote {
    void onScoreboardUpdate(int contestId) throws RemoteException;
}
