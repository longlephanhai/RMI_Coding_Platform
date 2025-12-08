package com.rmi.coding.platform.client.compponents;

import com.rmi.coding.platform.model.Contest;
import com.rmi.coding.platform.service.ContestService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ContestListPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private ContestService contestService;

    public ContestListPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Contest List");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);

        // =========================
        // Thêm cột Status
        // =========================
        String[] columns = {"ID", "Title", "Start", "End", "Status", "Action"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 5; // chỉ nút Action
            }
        };

        table = new JTable(model);
        table.setRowHeight(32);

        // Renderer
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        loadContests();
    }

    private void loadContests() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            contestService = (ContestService) registry.lookup("ContestService");

            List<Contest> contests = contestService.getContests();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            model.setRowCount(0);

            LocalDateTime now = LocalDateTime.now();

            if (contests != null) {
                for (Contest c : contests) {

                    String status;

                    if (now.isBefore(c.getStartTime())) {
                        status = "Not Started";
                    } else if (now.isAfter(c.getEndTime())) {
                        status = "Ended";
                    } else {
                        status = "Ongoing";
                    }

                    model.addRow(new Object[]{
                            c.getId(),
                            c.getTitle(),
                            c.getStartTime().format(fmt),
                            c.getEndTime().format(fmt),
                            status,
                            "View"
                    });
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Cannot load contests", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ----------- Button Renderer -------------
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
            String status = (String) table.getValueAt(row, 4);

            setText("View");

            if (status.equals("Ongoing")) {
                setEnabled(true);
                setBackground(new Color(66, 133, 244));
                setForeground(Color.WHITE);
            } else {
                setEnabled(false);
                setBackground(new Color(180, 180, 180));
                setForeground(Color.DARK_GRAY);
            }

            return this;
        }
    }

    // ----------- Button Editor -------------
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private boolean clicked;
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

            String status = (String) table.getValueAt(row, 4);

            button.setText("View");

            if (status.equals("Ongoing")) {
                button.setEnabled(true);
                button.setBackground(new Color(66, 133, 244));
                button.setForeground(Color.WHITE);
            } else {
                button.setEnabled(false);
                button.setBackground(new Color(180, 180, 180));
                button.setForeground(Color.DARK_GRAY);
            }

            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked && button.isEnabled()) {

                int contestId = (int) table.getValueAt(row, 0);
                String title = (String) table.getValueAt(row, 1);

                // TODO mở contest UI
                JOptionPane.showMessageDialog(button,
                        "Opening Contest: " + title + " (ID: " + contestId + ")");
            }

            clicked = false;
            return "View";
        }

        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }
}
