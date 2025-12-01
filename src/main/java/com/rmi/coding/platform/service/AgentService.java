package com.rmi.coding.platform.service;

import com.rmi.coding.platform.common.Agent;
import com.rmi.coding.platform.common.AgentCallback;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AgentService extends Remote {
    void submitAgent(Agent agent, AgentCallback callback) throws RemoteException;
}
