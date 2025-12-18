package com.rmi.coding.platform.service;

import com.rmi.coding.platform.common.ScoreboardCallback;
import com.rmi.coding.platform.config.DatabaseConnection;
import com.rmi.coding.platform.model.Scoreboard;
import com.rmi.coding.platform.repository.ScoreboardRepository;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ScoreboardServiceImpl extends UnicastRemoteObject implements ScoreboardService {

    private final ScoreboardRepository scoreboardRepository;
    private final Map<Integer, List<ScoreboardCallback>> callbacks = new ConcurrentHashMap<>();

    public ScoreboardServiceImpl() throws RemoteException {
        super();
        try {
            Connection conn = DatabaseConnection.getConnection();
            scoreboardRepository = new ScoreboardRepository(conn);
        } catch (Exception e) {
            throw new RemoteException("DB Connection Error", e);
        }
    }

    @Override
    public List<Scoreboard> getScoreboard(int contestId)
            throws RemoteException {

        List<Map<String, Object>> raw = scoreboardRepository.fetchRawScoreboard(contestId);

        Map<Integer, Scoreboard> map = new HashMap<>();

        for (Map<String, Object> r : raw) {
            int userId = (int) r.get("userId");
            String username = (String) r.get("username");
            int problemId = (int) r.get("problemId");
            int score = (int) r.get("score");

            map.putIfAbsent(userId, new Scoreboard());
            Scoreboard row = map.get(userId);

            row.setUserId(userId);
            row.setUsername(username);

            if (row.getProblemScores() == null)
                row.setProblemScores(new HashMap<>());

            row.getProblemScores().put(problemId, score);
        }

        // tính total
        for (Scoreboard r : map.values()) {
            int total = r.getProblemScores()
                    .values()
                    .stream()
                    .mapToInt(Integer::intValue)
                    .sum();
            r.setTotalScore(total);
        }

        return new ArrayList<>(map.values());
    }

    @Override
    public void registerCallback(int contestId, ScoreboardCallback callback) throws RemoteException {
        callbacks
                .computeIfAbsent(contestId, k -> new CopyOnWriteArrayList<>())
                .add(callback);
    }

    @Override
    public void unregisterCallback(int contestId, ScoreboardCallback callback) throws RemoteException {
        List<ScoreboardCallback> list = callbacks.get(contestId);
        if (list != null) list.remove(callback);
    }

    @Override
    public void notifyScoreboardChanged(int contestId) {
        List<ScoreboardCallback> list = callbacks.get(contestId);
        if (list == null) return;

        for (ScoreboardCallback cb : list) {
            try {
                cb.onScoreboardUpdate(contestId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
