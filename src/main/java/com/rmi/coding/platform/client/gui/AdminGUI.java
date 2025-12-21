package com.rmi.coding.platform.client.gui;

import com.rmi.coding.platform.model.Problem;
import com.rmi.coding.platform.model.TestCase;
import com.rmi.coding.platform.model.Contest;
import com.rmi.coding.platform.service.ContestService;
import com.rmi.coding.platform.service.ProblemService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

public class AdminGUI extends JFrame {

    private final ContestService contestService;
    private final ProblemService problemService;

    public AdminGUI(ContestService contestService, ProblemService problemService) {
        this.contestService = contestService;
        this.problemService = problemService;

        setTitle("Admin Panel");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 245, 245));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tabs.setBackground(new Color(245, 245, 245));

        tabs.addTab("Create Contest", createContestTab());
        tabs.addTab("Create Problem", createProblemTab());

        add(tabs, BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel createContestTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField contestTitle = new JTextField();
        SpinnerDateModel startModel = new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE);
        JSpinner startTime = new JSpinner(startModel);
        startTime.setEditor(new JSpinner.DateEditor(startTime, "yyyy-MM-dd HH:mm"));
        SpinnerDateModel endModel = new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE);
        JSpinner endTime = new JSpinner(endModel);
        endTime.setEditor(new JSpinner.DateEditor(endTime, "yyyy-MM-dd HH:mm"));

        String[] columns = {"Select", "ID", "Title", "Difficulty"};
        DefaultTableModel problemTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };
        JTable problemTable = styleTable(new JTable(problemTableModel));
        JScrollPane problemScroll = new JScrollPane(problemTable);
        problemScroll.setPreferredSize(new Dimension(600, 200));

        JLabel selectedCountLabel = new JLabel("Selected problems: 0");
        JButton refreshBtn = new JButton("Refresh Problems");
        styleButton(refreshBtn, new Color(255, 165, 0));

        JPanel tableTopPanel = new JPanel(new BorderLayout());
        tableTopPanel.setBackground(Color.WHITE);
        tableTopPanel.add(selectedCountLabel, BorderLayout.WEST);
        tableTopPanel.add(refreshBtn, BorderLayout.EAST);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(tableTopPanel, BorderLayout.NORTH);
        tablePanel.add(problemScroll, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formCard.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        formCard.add(contestTitle, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        formCard.add(new JLabel("Start Time:"), gbc);
        gbc.gridx = 1;
        formCard.add(startTime, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        formCard.add(new JLabel("End Time:"), gbc);
        gbc.gridx = 1;
        formCard.add(endTime, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        formCard.add(new JLabel("Select Problems:"), gbc);
        gbc.gridx = 1;
        formCard.add(tablePanel, gbc);

        JButton createContestBtn = new JButton("Create Contest");
        styleButton(createContestBtn, new Color(100, 149, 237));
        gbc.gridx = 1;
        gbc.gridy = 4;
        formCard.add(createContestBtn, gbc);

        panel.add(formCard);

        problemTableModel.addTableModelListener(e -> {
            int count = 0;
            for (int i = 0; i < problemTableModel.getRowCount(); i++) {
                if ((Boolean) problemTableModel.getValueAt(i, 0)) count++;
            }
            selectedCountLabel.setText("Selected problems: " + count);
        });

        loadProblems(problemTableModel);

        refreshBtn.addActionListener(e -> loadProblems(problemTableModel));

        createContestBtn.addActionListener(e -> {
            try {
                String title = contestTitle.getText().trim();
                Date startDate = (Date) startTime.getValue();
                Date endDate = (Date) endTime.getValue();
                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Contest title cannot be empty!");
                    return;
                }
                if (!startDate.before(endDate)) {
                    JOptionPane.showMessageDialog(this, "Start time must be before end time!");
                    return;
                }

                List<Integer> selectedProblemIds = new ArrayList<>();
                for (int i = 0; i < problemTableModel.getRowCount(); i++) {
                    if ((Boolean) problemTableModel.getValueAt(i, 0)) {
                        selectedProblemIds.add(Integer.parseInt((String) problemTableModel.getValueAt(i, 1)));
                    }
                }
                if (selectedProblemIds.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Select at least one problem!");
                    return;
                }

                Contest contest = new Contest();
                contest.setTitle(title);
                contest.setStartTime(LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault()));
                contest.setEndTime(LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault()));
                contest.setProblemIds(selectedProblemIds);

                contestService.createContest(contest);
                JOptionPane.showMessageDialog(this, "Contest created!");

                // Reset form
                contestTitle.setText("");
                startTime.setValue(new Date());
                endTime.setValue(new Date());
                for (int i = 0; i < problemTableModel.getRowCount(); i++)
                    problemTableModel.setValueAt(false, i, 0);
                selectedCountLabel.setText("Selected problems: 0");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error creating contest: " + ex.getMessage());
            }
        });

        return panel;
    }

    private void loadProblems(DefaultTableModel problemTableModel) {
        try {
            problemTableModel.setRowCount(0);
            List<Problem> allProblems = problemService.listAllProblems();
            for (Problem p : allProblems) {
                problemTableModel.addRow(new Object[]{false, String.valueOf(p.getId()), p.getTitle(), p.getDifficulty()});
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading problems: " + e.getMessage());
        }
    }

    private JPanel createProblemTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField problemTitleField = new JTextField();
        JTextArea description = new JTextArea(3, 20);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        JComboBox<String> difficulty = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});

        // Starter code tabs
        JTabbedPane starterTabs = new JTabbedPane();
        Map<String, JTextArea> starterAreas = new LinkedHashMap<>();
        for (String lang : List.of("Python", "JavaScript")) {
            JTextArea area = new JTextArea(8, 40);
            area.setFont(new Font("Monospaced", Font.PLAIN, 12));
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            starterAreas.put(lang.toLowerCase(), area);
            starterTabs.add(lang, new JScrollPane(area));
        }

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; formPanel.add(problemTitleField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; formPanel.add(new JScrollPane(description), gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Difficulty:"), gbc);
        gbc.gridx = 1; formPanel.add(difficulty, gbc);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Starter Code:"), gbc);
        gbc.gridx = 1; formPanel.add(starterTabs, gbc);

        // Test case table
        String[] testColumns = {"Input", "Expected Output"};
        DefaultTableModel testCaseModel = new DefaultTableModel(testColumns, 0);
        JTable testCaseTable = styleTable(new JTable(testCaseModel));
        JScrollPane testCaseScroll = new JScrollPane(testCaseTable);
        testCaseScroll.setPreferredSize(new Dimension(600, 200));

        JButton addTestCaseBtn = new JButton("Add Test Case");
        JButton removeTestCaseBtn = new JButton("Remove Selected");
        JButton addProblemBtn = new JButton("Add Problem");
        styleButton(addTestCaseBtn, new Color(60, 179, 113));
        styleButton(removeTestCaseBtn, new Color(220, 20, 60));
        styleButton(addProblemBtn, new Color(100, 149, 237));

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(new Color(245, 245, 245));
        btnPanel.add(addTestCaseBtn);
        btnPanel.add(removeTestCaseBtn);
        btnPanel.add(addProblemBtn);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(testCaseScroll, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        // Add Test Case
        addTestCaseBtn.addActionListener(e -> {
            String inputJson = JOptionPane.showInputDialog(this, "Test Case Input (JSON):");
            String expectedJson = JOptionPane.showInputDialog(this, "Expected Output (JSON):");
            if (inputJson != null && expectedJson != null) {
                try {
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(inputJson);
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(expectedJson);
                    testCaseModel.addRow(new Object[]{inputJson, expectedJson});
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid JSON!");
                }
            }
        });

        removeTestCaseBtn.addActionListener(e -> {
            int row = testCaseTable.getSelectedRow();
            if (row != -1) testCaseModel.removeRow(row);
        });

        addProblemBtn.addActionListener(e -> {
            try {
                Problem problem = new Problem();
                problem.setTitle(problemTitleField.getText());
                problem.setDescription(description.getText());
                problem.setDifficulty((String) difficulty.getSelectedItem());

                Map<String, String> starterMap = new HashMap<>();
                for (String lang : starterAreas.keySet()) starterMap.put(lang, starterAreas.get(lang).getText());
                problem.setStarterCode(starterMap);

                List<TestCase> testCases = new ArrayList<>();
                for (int i = 0; i < testCaseModel.getRowCount(); i++) {
                    TestCase tc = new TestCase();
                    tc.setInput((String) testCaseModel.getValueAt(i, 0));
                    tc.setExpectedOutput((String) testCaseModel.getValueAt(i, 1));
                    testCases.add(tc);
                }
                problem.setTestCases(testCases);

                int id = problemService.createProblem(problem);
                JOptionPane.showMessageDialog(this, "Problem added! ID: " + id);

                // Reset form
                problemTitleField.setText("");
                description.setText("");
                difficulty.setSelectedIndex(0);
                for (JTextArea area : starterAreas.values()) area.setText("");
                testCaseModel.setRowCount(0);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        return panel;
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    }

    private JTable styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(26);
        table.setShowGrid(true);
        table.setGridColor(new Color(220, 220, 220));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(230, 230, 230));
        table.getTableHeader().setForeground(Color.BLACK);
        return table;
    }
}
