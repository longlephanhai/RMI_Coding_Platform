package com.rmi.coding.platform.client.compponents;

import com.rmi.coding.platform.model.Submission;
import com.rmi.coding.platform.model.User;
import com.rmi.coding.platform.service.SubmissionService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;

public class HistorySubmitPanel extends JPanel {

    private final User user;
    private int currentProblemId;
    private DefaultTableModel tableModel;

    public HistorySubmitPanel(User user, int currentProblemId) {
        this.user = user;
        this.currentProblemId = currentProblemId;

        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("History Submissions", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);


        tableModel = new DefaultTableModel(
                new String[]{
                        "ID", "Language", "Passed", "Test Cases", "Time (ms)", "Submit Time"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        historyTable.setRowHeight(28);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Submission History"));

        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadData() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

//        Object[][] fakeData = new Object[][]{
//                {1, "python", true, "10/10", 120, LocalDateTime.now().minusMinutes(10).format(fmt)},
//                {2, "javascript", false, "6/10", 230, LocalDateTime.now().minusHours(1).format(fmt)},
//                {3, "python", true, "10/10", 98, LocalDateTime.now().minusHours(2).format(fmt)},
//                {4, "python", false, "4/10", 400, LocalDateTime.now().minusDays(1).format(fmt)},
//        };

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            SubmissionService submissionService = (SubmissionService) registry.lookup("SubmissionService");
            List<Submission> submissions = submissionService.getSubmissionsByUserAndProblem(user.getId(), currentProblemId);
            tableModel.setRowCount(0);
            for (Submission submission : submissions) {
                tableModel.addRow(new Object[]{
                        submission.getId(),
                        submission.getLanguage(),
                        submission.isPassed(),
                        submission.getPassedTests() + "/" + submission.getTotalTests(),
                        submission.getExecutionTime(),
                        submission.getSubmittedAt().format(fmt)
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Cannot load history submission");
        }


    }

    public void setCurrentProblemId(int id) {
        this.currentProblemId = id;
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        loadData();
    }
}
