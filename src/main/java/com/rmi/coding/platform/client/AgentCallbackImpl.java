package com.rmi.coding.platform.client;

import com.rmi.coding.platform.common.AgentCallback;
import javax.swing.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

public class AgentCallbackImpl extends UnicastRemoteObject implements AgentCallback {

    private final JTextArea outputArea;

    public AgentCallbackImpl(JTextArea outputArea) throws RemoteException {
        super();
        this.outputArea = outputArea;
    }

    @Override
    public void onResult(String result) throws RemoteException {
        SwingUtilities.invokeLater(() -> outputArea.setText(result));
    }

    @Override
    public void onError(String error) throws RemoteException {
        SwingUtilities.invokeLater(() -> outputArea.setText("[Error] " + error));
    }
}
