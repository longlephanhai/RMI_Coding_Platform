package com.rmi.coding.platform.service;

import com.rmi.coding.platform.common.Agent;
import com.rmi.coding.platform.common.AgentCallback;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class AgentServiceImpl extends UnicastRemoteObject implements AgentService {

    public AgentServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public void submitAgent(Agent agent, AgentCallback callback) throws RemoteException {
        System.out.println("Received agent submission");

        new Thread(() -> {
            try {
                System.out.println("Executing agent in thread: " + Thread.currentThread().getId());
                agent.execute(callback);
                System.out.println("Agent execution completed");
            } catch (Exception e) {
                System.err.println("Error executing agent: " + e.getMessage());
                e.printStackTrace();
                try {
                    callback.onError("Server execution error: " + e.getMessage());
                } catch (Exception ex) {
                    System.err.println("Failed to send error callback: " + ex.getMessage());
                }
            }
        }).start();

        System.out.println("Thread started for agent execution");
    }
}
