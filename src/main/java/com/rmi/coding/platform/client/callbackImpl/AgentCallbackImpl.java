package com.rmi.coding.platform.client.callbackImpl;

import com.rmi.coding.platform.agents.result.ScriptResult;
import com.rmi.coding.platform.common.AgentCallback;
import com.rmi.coding.platform.service.ScoreboardService;
import com.rmi.coding.platform.service.SubmissionService;

import javax.swing.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class AgentCallbackImpl extends UnicastRemoteObject implements AgentCallback {

    private final JTextArea outputArea;

    private final Integer contestId;
    private final int userId;
    private final int problemId;
    private final String language;
    private final String sourceCode;

    private SubmissionService submissionService;

    public AgentCallbackImpl(
            JTextArea outputArea,
            Integer contestId,
            int userId,
            int problemId,
            String language,
            String sourceCode
    ) throws RemoteException {
        super();
        this.outputArea = outputArea;

        this.contestId = contestId;
        this.userId = userId;
        this.problemId = problemId;
        this.language = language;
        this.sourceCode = sourceCode;
    }

    private synchronized SubmissionService getSubmissionService() throws Exception {
        if (submissionService == null) {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            submissionService = (SubmissionService) registry.lookup("SubmissionService");
        }
        return submissionService;
    }

    @Override
    public void onResult(ScriptResult result) throws RemoteException {

        SwingUtilities.invokeLater(() -> {
            String out = "[Result] Passed: "
                    + result.getPassedCount() + "/" + result.getTotalCount()
                    + "\nTime: " + result.getExecutionTime() + " ms\n\n"
                    + result.getRawOutput();

            outputArea.setText(out);
        });

        try {
            SubmissionService svc = getSubmissionService();

            svc.submitWithResult(
                    contestId,
                    userId,
                    problemId,
                    language,
                    sourceCode,
                    result
            );
            Registry reg = LocateRegistry.getRegistry();
            ScoreboardService service =
                    (ScoreboardService) reg.lookup("ScoreboardService");

            service.notifyScoreboardChanged(contestId);
        } catch (Exception ex) {
            SwingUtilities.invokeLater(() ->
                    outputArea.append("\n[Save Error] " + ex.getMessage())
            );
        }
    }

    @Override
    public void onError(String error) throws RemoteException {

        SwingUtilities.invokeLater(() ->
                outputArea.setText("[Error] " + error)
        );

        // Lưu kết quả lỗi
        try {
            SubmissionService svc = getSubmissionService();

            ScriptResult result = new ScriptResult();
            result.setPassedAll(false);
            result.setPassedCount(0);
            result.setTotalCount(0);
            result.setExecutionTime(0);
            result.setRawOutput(error);

            svc.submitWithResult(
                    contestId,
                    userId,
                    problemId,
                    language,
                    sourceCode,
                    result
            );

        } catch (Exception ex) {
            SwingUtilities.invokeLater(() ->
                    outputArea.append("\n[Save Error] " + ex.getMessage())
            );
        }
    }
}
