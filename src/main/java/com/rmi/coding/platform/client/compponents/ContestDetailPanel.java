package com.rmi.coding.platform.client.compponents;

import com.rmi.coding.platform.model.Problem;
import com.rmi.coding.platform.model.User;
import com.rmi.coding.platform.service.ContestParticipantService;
import com.rmi.coding.platform.service.ContestService;
import com.rmi.coding.platform.service.ProblemService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class ContestDetailPanel extends JPanel {

    private final int contestId;
    private final User user;

    // ===== Problems =====
    private JTable problemTable;
    private DefaultTableModel problemModel;

    public ContestDetailPanel(int contestId, User user) {
        this.contestId = contestId;
        this.user = user;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Contest " + contestId);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        add(createMainContent(), BorderLayout.CENTER);

        autoJoinContest();
        loadProblems();
    }

    // ================= MAIN LAYOUT =================

    private JComponent createMainContent() {
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                createProblemPanel(),
                new ScoreboardPanel(contestId) // ✅ SCOREBOARD Ở ĐÂY
        );

        splitPane.setDividerLocation(650);
        splitPane.setResizeWeight(0.6);
        splitPane.setOneTouchExpandable(true);

        return splitPane;
    }

    // ================= PROBLEMS =================

    private JPanel createProblemPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JLabel label = new JLabel("Problems");
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(label, BorderLayout.NORTH);

        String[] columns = {"ID", "Title", "Difficulty", "Action"};

        problemModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 3;
            }
        };

        problemTable = new JTable(problemModel);
        problemTable.setRowHeight(32);

        problemTable.getColumn("Action")
                .setCellRenderer(new ButtonRenderer());
        problemTable.getColumn("Action")
                .setCellEditor(new ButtonEditor(new JCheckBox()));

        panel.add(new JScrollPane(problemTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadProblems() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ContestService contestService =
                    (ContestService) registry.lookup("ContestService");

            List<Problem> problems = contestService.getProblems(contestId);

            problemModel.setRowCount(0);
            for (Problem p : problems) {
                problemModel.addRow(new Object[]{
                        p.getId(), p.getTitle(), p.getDifficulty(), "Open"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= AUTO JOIN =================

    private void autoJoinContest() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ContestParticipantService service =
                    (ContestParticipantService)
                            registry.lookup("ContestParticipantService");

            service.joinContest(contestId, user.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= BUTTONS =================

    static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column
        ) {
            setText("Open");
            setBackground(new Color(66, 133, 244));
            setForeground(Color.WHITE);
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {

        private final JButton button;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);

            button = new JButton("Open");
            button.setBackground(new Color(66, 133, 244));
            button.setForeground(Color.WHITE);
            button.setOpaque(true);

            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value,
                boolean isSelected, int row, int column
        ) {
            this.row = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            int problemId = (int) problemTable.getValueAt(row, 0);
            String title = (String) problemTable.getValueAt(row, 1);

            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Problem: " + title);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                try {
                    Registry registry =
                            LocateRegistry.getRegistry("localhost", 1099);
                    ProblemService problemService =
                            (ProblemService) registry.lookup("ProblemService");

                    Problem problem = problemService.listAllProblems()
                            .stream()
                            .filter(p -> p.getId() == problemId)
                            .findFirst()
                            .orElse(null);

                    MiniScriptIDEContestPanel panel =
                            new MiniScriptIDEContestPanel(user, contestId);

                    if (problem != null) {
                        panel.setStarterCode(problem.getStarterCode());
                        panel.setCurrentProblemId(problem.getId());
                    }

                    frame.add(panel);
                    frame.setVisible(true);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            return "Open";
        }
    }
}
