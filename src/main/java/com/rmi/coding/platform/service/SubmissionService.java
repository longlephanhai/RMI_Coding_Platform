package com.rmi.coding.platform.service;

import com.rmi.coding.platform.agents.result.ScriptResult;
import com.rmi.coding.platform.model.Submission;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface SubmissionService extends Remote {

    Submission submit(int userId, int problemId, String language, String code) throws RemoteException;

    Submission submitInContest(int contestId, int userId, int problemId, String language, String code) throws RemoteException;


    Submission submitWithResult(Integer contestId,
                                int userId,
                                int problemId,
                                String language,
                                String code,
                                ScriptResult result) throws RemoteException;

    List<Submission> getSubmissionsByUser(int userId) throws RemoteException;

    List<Submission> getSubmissionsByProblem(int problemId) throws RemoteException;

    List<Submission> getSubmissionsByContest(int contestId) throws RemoteException;

    Submission getLatestSubmissionInContest(int userId, int contestId) throws RemoteException;

    Submission getSubmissionById(int id) throws RemoteException;
}
