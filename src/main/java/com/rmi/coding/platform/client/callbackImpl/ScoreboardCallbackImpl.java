package com.rmi.coding.platform.client.callbackImpl;

import com.rmi.coding.platform.client.compponents.ScoreboardPanel;
import com.rmi.coding.platform.common.ScoreboardCallback;

import javax.swing.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ScoreboardCallbackImpl
        extends UnicastRemoteObject
        implements ScoreboardCallback {

    private final ScoreboardPanel panel;
    private final int contestId;

    public ScoreboardCallbackImpl(ScoreboardPanel panel, int contestId)
            throws RemoteException {
        this.panel = panel;
        this.contestId = contestId;
    }

    @Override
    public void onScoreboardUpdate(int contestId) throws RemoteException {
        if (this.contestId != contestId) return;

        SwingUtilities.invokeLater(panel::load);
    }
}
