package com.rmi.coding.platform.client;

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

public class ClientGUI extends JFrame {

    private final UserService userService;
    private User loggedUser;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    public ClientGUI(UserService userService) {
        this.userService = userService;

        setTitle("Coding Platform");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Panels login/register
        LoginGUI loginGUI = new LoginGUI(this.userService, this);
        RegisterGUI registerGUI = new RegisterGUI(this.userService, this);

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

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(245, 245, 245));

        // Header welcome
        JLabel label = new JLabel("Welcome, " + loggedUser.getUsername() + "! Role: " + loggedUser.getRole());
        label.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(label, BorderLayout.NORTH);

        // Table Problem
        String[] columns = {"ID", "Title", "Difficulty", "Action"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);

        // Load problems từ server
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

        JScrollPane scroll = new JScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);

        add(panel);
        revalidate();
        repaint();
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
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
        private JButton button;
        private String label;
        private boolean clicked;
        private ClientGUI gui;
        private User user;
        private JTable table;

        public ButtonEditor(JCheckBox checkBox, ClientGUI gui, User user, JTable table) {
            super(checkBox);
            this.gui = gui;
            this.user = user;
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
                        MiniScriptIDEPanel idePanel = new MiniScriptIDEPanel();
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
