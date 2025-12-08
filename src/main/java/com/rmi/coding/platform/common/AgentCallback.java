package com.rmi.coding.platform.common;

import com.rmi.coding.platform.agents.result.ScriptResult;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AgentCallback extends Remote {
    void onResult(ScriptResult result) throws RemoteException;

    void onError(String error) throws RemoteException;
}
