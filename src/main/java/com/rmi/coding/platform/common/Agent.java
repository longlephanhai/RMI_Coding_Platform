package com.rmi.coding.platform.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Agent extends Remote {
    void execute(AgentCallback callback) throws RemoteException;
}
