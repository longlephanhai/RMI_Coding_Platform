package com.rmi.coding.platform.client.compponents;

import com.rmi.coding.platform.client.callbackImpl.ScoreboardCallbackImpl;
import com.rmi.coding.platform.common.ScoreboardCallback;
import com.rmi.coding.platform.model.Scoreboard;

import com.rmi.coding.platform.service.ScoreboardService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.List;

public class ScoreboardPanel extends JPanel {

    private final int contestId;

    private DefaultTableModel model;

    private ScoreboardService scoreboardService;
    private ScoreboardCallback callback;

    public ScoreboardPanel(int contestId) {
        this.contestId = contestId;

        setLayout(new BorderLayout(5, 5));
        initUI();
        initRMI();
        load();
        registerCallback();
    }


    private void initUI() {
        JLabel title = new JLabel("Scoreboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel();
        JTable table = new JTable(model);
        table.setRowHeight(28);

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void initRMI() {
        try {
            Registry reg = LocateRegistry.getRegistry("localhost", 1099);
            scoreboardService =
                    (ScoreboardService) reg.lookup("ScoreboardService");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void load() {
        try {
            List<Scoreboard> rows =
                    scoreboardService.getScoreboard(contestId);

            refreshTable(rows);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void refreshTable(List<Scoreboard> rows) {

        // collect all problemIds
        Set<Integer> problems = new TreeSet<>();
        for (Scoreboard r : rows) {
            problems.addAll(r.getProblemScores().keySet());
        }

        // columns
        Vector<String> cols = new Vector<>();
        cols.add("User");
        for (int p : problems) cols.add("P" + p);
        cols.add("Total");

        Vector<Vector<Object>> data = new Vector<>();

        for (Scoreboard r : rows) {
            Vector<Object> row = new Vector<>();
            row.add(r.getUsername());

            for (int p : problems) {
                row.add(r.getProblemScores().getOrDefault(p, 0));
            }

            row.add(r.getTotalScore());
            data.add(row);
        }

        model.setDataVector(data, cols);
    }


    private void registerCallback() {
        try {
            callback = new ScoreboardCallbackImpl(this, contestId);
            scoreboardService.registerCallback(contestId, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unregisterCallback() {
        try {
            if (callback != null) {
                scoreboardService.unregisterCallback(contestId, callback);
            }
        } catch (Exception ignored) {
        }
    }


    @Override
    public void removeNotify() {
        unregisterCallback();
        super.removeNotify();
    }
}
