package com.rmi.coding.platform.server;

import com.rmi.coding.platform.service.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        try {

            Registry registry = LocateRegistry.createRegistry(1099);

            UserService userService = new UserServiceImpl();
            ContestService contestService = new ContestServiceImpl();
            ProblemService problemService = new ProblemServiceImpl();
            AgentService agentService = new AgentServiceImpl();
            TestCaseService testCaseService = new TestCaseServiceImpl();
            SubmissionService submissionService = new SubmissionServiceImpl();
            ContestParticipantService contestParticipantService = new ContestParticipantServiceImpl();

            registry.rebind("UserService", userService);
            registry.rebind("ContestService", contestService);
            registry.rebind("ProblemService", problemService);
            registry.rebind("AgentService", agentService);
            registry.rebind("TestCaseService", testCaseService);
            registry.rebind("SubmissionService", submissionService);
            registry.rebind("ContestParticipantService", contestParticipantService);

            System.out.println("RMI Server is running on port 1099...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
