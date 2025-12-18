package com.rmi.coding.platform.client.compponents;

import com.rmi.coding.platform.model.Submission;
import com.rmi.coding.platform.model.User;
import com.rmi.coding.platform.service.SubmissionService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistorySubmitPanel extends JPanel {

    private final User user;
    private int currentProblemId;
    private DefaultTableModel tableModel;
    private JTable historyTable;

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

        // TABLE MODEL
        tableModel = new DefaultTableModel(
                new Object[]{
                        "ID", "Language", "Passed", "Test Cases", "Time (ms)", "Submit Time", "View Code"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // chỉ cột View Code được click
            }
        };

        historyTable = new JTable(tableModel);

        historyTable.setRowHeight(28);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        addButtonRendererAndEditor();

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Submission History"));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addButtonRendererAndEditor() {
        // Renderer cho nút
        historyTable.getColumn("View Code").setCellRenderer(new TableCellRenderer() {
            private final JButton btn = new JButton("View");

            @Override
            public Component getTableCellRendererComponent(JTable table, Object o, boolean isSelected, boolean hasFocus, int row, int col) {
                btn.setBackground(new Color(60, 130, 200));
                btn.setForeground(Color.WHITE);
                return btn;
            }
        });

        // Editor cho nút
        historyTable.getColumn("View Code").setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private final JButton button = new JButton("View");
            private int selectedRow;

            {
                button.addActionListener((ActionEvent e) -> {
                    openCodeViewer(selectedRow);
                });
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
                selectedRow = row;
                return button;
            }
        });
    }

    private void openCodeViewer(int row) {
        int submissionId = (int) tableModel.getValueAt(row, 0);

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            SubmissionService submissionService = (SubmissionService) registry.lookup("SubmissionService");

            Submission submission = submissionService.getSubmissionById(submissionId);

            // Create dialog
            JDialog dialog = new JDialog((Frame) null, "View Code - ID: " + submissionId, true);
            dialog.setSize(650, 500);
            dialog.setLocationRelativeTo(null);

            JTextArea codeArea = new JTextArea(submission.getCode());
            codeArea.setFont(new Font("Consolas", Font.PLAIN, 14));
            codeArea.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(codeArea);

            dialog.add(scrollPane);
            dialog.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Cannot load code!");
        }
    }

    private void loadData() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            SubmissionService submissionService = (SubmissionService) registry.lookup("SubmissionService");
            List<Submission> submissions = submissionService.getSubmissionsByUserAndProblem(user.getId(), currentProblemId);

            tableModel.setRowCount(0);

            for (Submission s : submissions) {
                tableModel.addRow(new Object[]{
                        s.getId(),
                        s.getLanguage(),
                        s.isPassed(),
                        s.getPassedTests() + "/" + s.getTotalTests(),
                        s.getExecutionTime(),
                        s.getSubmittedAt().format(fmt),
                        "View"
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Cannot load history submission");
        }
    }

    public void setCurrentProblemId(int id) {
        this.currentProblemId = id;
        loadData();
    }
}
