package com.rmi.coding.platform.client.compponents;


import com.rmi.coding.platform.model.Problem;
import com.rmi.coding.platform.model.User;
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
    private final JTable table;
    private final DefaultTableModel model;

    public ContestDetailPanel(int contestId, User user) {
        this.contestId = contestId;
        this.user = user;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Problems in Contest " + contestId);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        String[] columns = {"ID", "Title", "Difficulty", "Action"};

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 3;
            }
        };

        table = new JTable(model);
        table.setRowHeight(32);

        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

        add(new JScrollPane(table), BorderLayout.CENTER);

        loadProblems();
    }

    private void loadProblems() {
        try {
            model.setRowCount(0);

            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ContestService contestService = (ContestService) registry.lookup("ContestService");

            List<Problem> problems = contestService.getProblems(contestId);

            System.out.println(problems);

            model.setRowCount(0);
            for (Problem p : problems) {
                model.addRow(new Object[]{p.getId(), p.getTitle(), p.getDifficulty(), "Open"});
            }


        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load problems.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


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
            button.setOpaque(true);
            button.setBackground(new Color(66, 133, 244));
            button.setForeground(Color.WHITE);

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

            int problemId = (int) table.getValueAt(row, 0);
            String title = (String) table.getValueAt(row, 1);

            SwingUtilities.invokeLater(() -> {

                JFrame frame = new JFrame("Problem: " + title);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(1000, 700);
                frame.setLocationRelativeTo(null);

                try {
                    Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                    ProblemService problemService = (ProblemService) registry.lookup("ProblemService");

                    Problem problem = problemService.listAllProblems()
                            .stream()
                            .filter(p -> p.getId() == problemId)
                            .findFirst()
                            .orElse(null);

                    MiniScriptIDEPanel panel = new MiniScriptIDEPanel(user);
                    assert problem != null;
                    panel.setStarterCode(problem.getStarterCode());
                    panel.setCurrentProblemId(problem.getId());
                    frame.add(panel,BorderLayout.CENTER);
                    frame.setVisible(true);

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error loading problem: " + e.getMessage());
                }
            });

            return "Open";
        }

    }
}
