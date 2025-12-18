package com.rmi.coding.platform.client.gui;

import com.rmi.coding.platform.client.compponents.ContestDetailPanel;
import com.rmi.coding.platform.client.compponents.MiniScriptIDEPanel;
import com.rmi.coding.platform.model.Problem;
import com.rmi.coding.platform.model.User;
import com.rmi.coding.platform.service.ContestService;
import com.rmi.coding.platform.service.ProblemService;
import com.rmi.coding.platform.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;

public class ClientGUI extends JFrame {

    private User loggedUser;
    private final JPanel mainPanel;
    private final CardLayout cardLayout;

    public ClientGUI(UserService userService) {

        setTitle("Coding Platform");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Panels login/register
        LoginGUI loginGUI = new LoginGUI(userService, this);
        RegisterGUI registerGUI = new RegisterGUI(userService, this);

        mainPanel.add(loginGUI, "login");
        mainPanel.add(registerGUI, "register");

        add(mainPanel);
        showLoginPanel();
    }

    public void showLoginPanel() {
        cardLayout.show(mainPanel, "login");
    }

    public void showRegisterPanel() {
        cardLayout.show(mainPanel, "register");
    }

    public void onLoginSuccess(User user) {
        this.loggedUser = user;

        if ("admin".equalsIgnoreCase(user.getRole())) {
            try {
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                ContestService contestService = (ContestService) registry.lookup("ContestService");
                ProblemService problemService = (ProblemService) registry.lookup("ProblemService");
                new AdminGUI(contestService, problemService);
                this.dispose();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Cannot connect to ContestService", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            showMainPanel();
        }
    }

    private void showMainPanel() {
        getContentPane().removeAll();

        // Wrapper chính
        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        wrapper.setBackground(new Color(245, 245, 245));

        // ========== HEADER ==========
        JLabel label = new JLabel("Welcome, " + loggedUser.getUsername() + "! Role: " + loggedUser.getRole());
        label.setFont(new Font("Arial", Font.BOLD, 22));
        wrapper.add(label, BorderLayout.NORTH);

        // ========== MENU ==========
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnProblems = new JButton("Problems");
        JButton btnContests = new JButton("Contests");
        menuPanel.add(btnProblems);
        menuPanel.add(btnContests);
        wrapper.add(menuPanel, BorderLayout.SOUTH);

        // ========== CARD LAYOUT ==========
        JPanel cardPanel = new JPanel(new CardLayout());
        CardLayout cl = (CardLayout) cardPanel.getLayout();

        // ============================================================
        //                    PANEL PROBLEMS (Nguyên bản)
        // ============================================================
        JPanel problemsPanel = new JPanel(new BorderLayout(10, 10));
        problemsPanel.setBackground(new Color(245, 245, 245));

        String[] columns = {"ID", "Title", "Difficulty", "Action"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(30);

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ProblemService problemService = (ProblemService) registry.lookup("ProblemService");
            problemService.listAllProblems().forEach(p -> {
                model.addRow(new Object[]{
                        p.getId(),
                        p.getTitle(),
                        p.getDifficulty(),
                        "Solve"
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Cannot load problems", "Error", JOptionPane.ERROR_MESSAGE);
        }

        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox(), this, loggedUser, table));

        problemsPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel contestPanel = new JPanel(new BorderLayout(10, 10));
        contestPanel.setBackground(new Color(245, 245, 245));

        JLabel ctTitle = new JLabel("Contest List");
        ctTitle.setFont(new Font("Arial", Font.BOLD, 20));
        contestPanel.add(ctTitle, BorderLayout.NORTH);

        String[] contestCol = {"ID", "Title", "Start", "End", "Status", "Action"};
        DefaultTableModel contestModel = new DefaultTableModel(contestCol, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 5;
            }
        };

        JTable contestTable = new JTable(contestModel);
        contestTable.setRowHeight(30);

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ContestService contestService = (ContestService) registry.lookup("ContestService");

            contestService.getContests().forEach(c -> {

                LocalDateTime now = LocalDateTime.now();
                String status;

                if (now.isBefore(c.getStartTime())) {
                    status = "Not started";
                } else if (now.isAfter(c.getEndTime())) {
                    status = "Ended";
                } else {
                    status = "Running";
                }

                contestModel.addRow(new Object[]{
                        c.getId(),
                        c.getTitle(),
                        c.getStartTime().toString(),
                        c.getEndTime().toString(),
                        status,
                        "Join"
                });
            });

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Cannot load contests", "Error", JOptionPane.ERROR_MESSAGE);
        }

        // renderer + editor
        contestTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        contestTable.getColumn("Action").setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            final JButton btn = new JButton("Join");
            boolean clicked = false;
            int row;

            {
                btn.addActionListener(e -> fireEditingStopped());
            }

            @Override
            public Component getTableCellEditorComponent(JTable tbl, Object val, boolean sel, int r, int col) {
                row = r;
                clicked = true;
                return btn;
            }

            @Override
            public Object getCellEditorValue() {
                if (clicked) {

                    int id = (int) contestTable.getValueAt(row, 0);
                    String title = (String) contestTable.getValueAt(row, 1);
                    String status = (String) contestTable.getValueAt(row, 4);

                    switch (status) {
                        case "Not started":
                            JOptionPane.showMessageDialog(btn,
                                    "Contest has not started yet!");
                            break;

                        case "Ended":
                            JOptionPane.showMessageDialog(btn,
                                    "Contest is already finished.");
                            break;

                        case "Running":
                            SwingUtilities.invokeLater(() -> {
                                JFrame frame = new JFrame("Contest: " + title);
                                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                                frame.setSize(900, 600);
                                frame.setLocationRelativeTo(null);

                                frame.add(new ContestDetailPanel(id, loggedUser));

                                frame.setVisible(true);
                            });
                            break;
                    }
                }
                clicked = false;
                return "Join";
            }

        });

        contestPanel.add(new JScrollPane(contestTable), BorderLayout.CENTER);

        // Add vào card
        cardPanel.add(problemsPanel, "problems");
        cardPanel.add(contestPanel, "contests");

        wrapper.add(cardPanel, BorderLayout.CENTER);

        // Switch tab
        btnProblems.addActionListener(e -> cl.show(cardPanel, "problems"));
        btnContests.addActionListener(e -> cl.show(cardPanel, "contests"));

        add(wrapper);
        revalidate();
        repaint();
    }


    static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private String label;
        private boolean clicked;
        private final JTable table;

        public ButtonEditor(JCheckBox checkBox, ClientGUI gui, User user, JTable table) {
            super(checkBox);
            this.table = table;

            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                int row = table.getSelectedRow();
                int problemId = (int) table.getValueAt(row, 0);

                try {
                    Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                    ProblemService problemService = (ProblemService) registry.lookup("ProblemService");
                    Problem problem = problemService.listAllProblems()
                            .stream()
                            .filter(p -> p.getId() == problemId)
                            .findFirst()
                            .orElse(null);

                    if (problem != null) {
                        JFrame ideFrame = new JFrame("Solve: " + problem.getTitle());
                        ideFrame.setSize(900, 700);
                        ideFrame.setLocationRelativeTo(null);

                        // Main panel
                        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
                        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                        // Description area
                        JTextArea descArea = new JTextArea(problem.getDescription());
                        descArea.setLineWrap(true);
                        descArea.setWrapStyleWord(true);
                        descArea.setEditable(false);
                        descArea.setFont(new Font("Arial", Font.PLAIN, 14));
                        descArea.setBackground(new Color(245, 245, 245));
                        JScrollPane descScroll = new JScrollPane(descArea);
                        descScroll.setBorder(BorderFactory.createTitledBorder("Description"));
                        descScroll.setPreferredSize(new Dimension(880, 150));
                        mainPanel.add(descScroll, BorderLayout.NORTH);

                        // Code editor panel
                        MiniScriptIDEPanel idePanel = new MiniScriptIDEPanel(loggedUser);
                        idePanel.setStarterCode(problem.getStarterCode());
                        idePanel.setCurrentProblemId(problem.getId());
                        mainPanel.add(idePanel, BorderLayout.CENTER);

                        ideFrame.add(mainPanel);
                        ideFrame.setVisible(true);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(button, "Cannot open problem: " + ex.getMessage());
                }
            }
            clicked = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

}
