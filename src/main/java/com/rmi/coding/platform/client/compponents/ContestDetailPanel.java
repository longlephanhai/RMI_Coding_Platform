package com.rmi.coding.platform.client.compponents;

import com.rmi.coding.platform.model.Contest;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class ContestDetailPanel extends JPanel {

    private final int contestId;
    private final User user;

    private JTable problemTable;
    private DefaultTableModel problemModel;

    private boolean contestRunning = false;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private final JLabel timeLabel = new JLabel();

    public ContestDetailPanel(int contestId, User user) {
        this.contestId = contestId;
        this.user = user;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Contest " + contestId);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(timeLabel, BorderLayout.SOUTH);

        add(createMainContent(), BorderLayout.CENTER);

        autoJoinContest();
        loadContest();
        startCountdown();
        loadProblems();
    }

    private JComponent createMainContent() {
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                createProblemPanel(),
                new ScoreboardPanel(contestId)
        );

        splitPane.setDividerLocation(650);
        splitPane.setResizeWeight(0.6);
        splitPane.setOneTouchExpandable(true);

        return splitPane;
    }

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

    private void loadContest() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ContestService contestService =
                    (ContestService) registry.lookup("ContestService");

            Contest contest = contestService.getContestById(contestId);

            startTime = contest.getStartTime();
            endTime = contest.getEndTime();

            LocalDateTime now = LocalDateTime.now();
            contestRunning = now.isAfter(startTime) && now.isBefore(endTime);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
                        p.getId(),
                        p.getTitle(),
                        p.getDifficulty(),
                        contestRunning ? "Open" : "Ended"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCountdown() {
        Timer timer = new Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();

            if (now.isBefore(startTime)) {
                timeLabel.setText("⏳ Contest chưa bắt đầu");
                return;
            }

            Duration d = Duration.between(now, endTime);

            if (d.isZero() || d.isNegative()) {
                contestRunning = false;
                timeLabel.setText("Contest đã kết thúc");
                problemTable.repaint();

                showToast("Contest đã kết thúc. Submit đã bị khóa.");

                ((Timer) e.getSource()).stop();
                return;
            }


            long h = d.toHours();
            long m = d.toMinutes() % 60;
            long s = d.getSeconds() % 60;

            contestRunning = true;
            timeLabel.setText(
                    String.format("Time left: %02d:%02d:%02d", h, m, s)
            );
            problemTable.repaint();
        });
        timer.start();
    }

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

    class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column
        ) {
            if (!contestRunning) {
                setText("Ended");
                setEnabled(false);
                setBackground(Color.LIGHT_GRAY);
                setForeground(Color.DARK_GRAY);
            } else {
                setText("Open");
                setEnabled(true);
                setBackground(new Color(66, 133, 244));
                setForeground(Color.WHITE);
            }
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {

        private final JButton button;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value,
                boolean isSelected, int row, int column
        ) {
            this.row = row;

            if (!contestRunning) {
                button.setText("Ended");
                button.setEnabled(false);
                button.setBackground(Color.LIGHT_GRAY);
                button.setForeground(Color.DARK_GRAY);
            } else {
                button.setText("Open");
                button.setEnabled(true);
                button.setBackground(new Color(66, 133, 244));
                button.setForeground(Color.WHITE);
            }
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (!contestRunning) {
                JOptionPane.showMessageDialog(
                        null,
                        "Contest đã kết thúc",
                        "Blocked",
                        JOptionPane.WARNING_MESSAGE
                );
                return "Ended";
            }

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

                    assert problem != null;
                    JTextArea descArea = new JTextArea(problem.getDescription());
                    descArea.setLineWrap(true);
                    descArea.setWrapStyleWord(true);
                    descArea.setEditable(false);
                    descArea.setFont(new Font("Arial", Font.PLAIN, 14));
                    descArea.setBackground(new Color(245, 245, 245));
                    JScrollPane descScroll = new JScrollPane(descArea);
                    descScroll.setBorder(BorderFactory.createTitledBorder("Description"));
                    descScroll.setPreferredSize(new Dimension(880, 150));
                    panel.add(descScroll, BorderLayout.NORTH);

                    panel.setStarterCode(problem.getStarterCode());
                    panel.setCurrentProblemId(problem.getId());

                    frame.add(panel);
                    frame.setVisible(true);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            return "Open";
        }
    }

    private void showToast(String message) {
        JWindow toast = new JWindow();

        JPanel panel = new JPanel();
        panel.setBackground(new Color(50, 50, 50));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel label = new JLabel(message);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        panel.add(label);
        toast.add(panel);
        toast.pack();

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screen.width - toast.getWidth() - 20;
        int y = 40;
        toast.setLocation(x, y);

        toast.setAlwaysOnTop(true);
        toast.setVisible(true);

        new Timer(4000, e -> toast.dispose()).start();
    }

}
