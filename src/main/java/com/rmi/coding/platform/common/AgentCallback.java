package com.rmi.coding.platform.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AgentCallback extends Remote {
    void onResult(String result) throws RemoteException;
    void onError(String error) throws RemoteException;
}
