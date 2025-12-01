package com.rmi.coding.platform.agents;

import com.rmi.coding.platform.common.Agent;
import com.rmi.coding.platform.common.AgentCallback;
import com.rmi.coding.platform.agents.tasks.ScriptTask;

import java.io.Serializable;
import java.rmi.RemoteException;

public class GenericAgent implements Serializable, Agent {

    private final Object task;

    public GenericAgent(Object task) {
        this.task = task;
    }

    @Override
    public void execute(AgentCallback callback) throws RemoteException {
        try {
            if (task instanceof ScriptTask) {
                ((ScriptTask) task).run(callback);
            } else if (task instanceof Runnable) {
                ((Runnable) task).run();
            } else {
                callback.onError("Unsupported task type");
            }
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
}
