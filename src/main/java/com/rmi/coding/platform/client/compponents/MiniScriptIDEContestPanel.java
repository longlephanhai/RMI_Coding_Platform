package com.rmi.coding.platform.client.compponents;

import com.rmi.coding.platform.agents.GenericAgent;
import com.rmi.coding.platform.agents.tasks.ScriptTask;
import com.rmi.coding.platform.client.AgentCallbackImpl;
import com.rmi.coding.platform.model.TestCase;
import com.rmi.coding.platform.model.User;
import com.rmi.coding.platform.service.AgentService;
import com.rmi.coding.platform.service.TestCaseService;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiniScriptIDEContestPanel extends JPanel {
    private RSyntaxTextArea codeArea;
    private JComboBox<String> langCombo;
    private Map<String, String> codeMap;
    private String currentLang;

    private JButton runButton;
    private JButton clearButton;
    private JButton historyButton;
    private JButton themeButton;
    private JTextArea outputArea;

    private boolean darkMode = false;

    private int currentProblemId;
    private final int contestId;
    private final User currentUser;

    public MiniScriptIDEContestPanel(User user, int contestId) {
        this.currentUser = user;
        this.contestId = contestId;
        initComponents();
        setupLayout();
        initializeDefaults();
        applyLightTheme();
    }


    private void initComponents() {
        codeMap = new HashMap<>();
        currentLang = "python";

        langCombo = new JComboBox<>(new String[]{"Python", "JavaScript"});
        langCombo.setSelectedItem("Python");
        langCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        langCombo.addActionListener(this::handleLanguageChange);

        themeButton = new JButton("Dark");
        themeButton.setFocusPainted(false);
        themeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        themeButton.addActionListener(e -> toggleTheme());

        codeArea = new RSyntaxTextArea();
        codeArea.setCodeFoldingEnabled(true);
        codeArea.setAntiAliasingEnabled(true);
        codeArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        codeArea.setTabSize(4);
        codeArea.setAutoIndentEnabled(true);
        codeArea.setHighlightCurrentLine(true);

        runButton = styledButton("Run", new Color(46, 204, 113));
        clearButton = styledButton("Reset", new Color(231, 76, 60));
        historyButton = styledButton("History Submit", new Color(231, 76, 60));

        runButton.addActionListener(e -> handleRun());
        clearButton.addActionListener(e -> handleClear());
        historyButton.addActionListener(e -> {
            HistorySubmitPanel historySubmitPanel = new HistorySubmitPanel(currentUser, currentProblemId);

            Container parent = MiniScriptIDEContestPanel.this.getParent();
            if (parent != null) {
                parent.removeAll();
                parent.add(historySubmitPanel);
                parent.revalidate();
                parent.repaint();
            }
        });


        outputArea = new JTextArea(10, 80);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        outputArea.setEditable(false);
        outputArea.setMargin(new Insets(10, 10, 10, 10));
    }

    private JButton styledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(100, 32));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return btn;
    }

    private void setupLayout() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topBar.add(new JLabel("Language: "));
        topBar.add(langCombo);
        topBar.add(runButton);
        topBar.add(clearButton);
        topBar.add(historyButton);
        topBar.add(themeButton);

        add(topBar, BorderLayout.NORTH);

        RTextScrollPane scrollPane = new RTextScrollPane(codeArea);
        scrollPane.setLineNumbersEnabled(true);
        scrollPane.setFoldIndicatorEnabled(true);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Editor"));

        add(scrollPane, BorderLayout.CENTER);

        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Output"));

        add(outputScroll, BorderLayout.SOUTH);
    }

    private void toggleTheme() {
        if (!darkMode) applyDarkTheme();
        else applyLightTheme();
        darkMode = !darkMode;
    }

    private void applyDarkTheme() {
        codeArea.setBackground(new Color(32, 32, 32));
        codeArea.setForeground(Color.WHITE);
        outputArea.setBackground(new Color(40, 40, 40));
        outputArea.setForeground(Color.WHITE);

        setBackground(new Color(25, 25, 25));

        themeButton.setText("Light");
    }

    private void applyLightTheme() {
        codeArea.setBackground(Color.WHITE);
        codeArea.setForeground(Color.BLACK);
        outputArea.setBackground(new Color(245, 245, 245));
        outputArea.setForeground(Color.BLACK);

        setBackground(new Color(250, 250, 250));

        themeButton.setText("Dark");
    }

    private void initializeDefaults() {
        setSyntax(currentLang);
        codeMap.put(currentLang, "");
    }

    private void handleLanguageChange(ActionEvent e) {
        String selected = ((String) langCombo.getSelectedItem()).toLowerCase();

        if (selected.equals(currentLang)) return;

        codeMap.put(currentLang, codeArea.getText());
        currentLang = selected;
        codeArea.setText(codeMap.getOrDefault(currentLang, ""));
        setSyntax(currentLang);
    }

    private void setSyntax(String lang) {
        if (lang.equals("python"))
            codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
        else if (lang.equals("javascript"))
            codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
    }

    private void handleRun() {
        codeMap.put(currentLang, codeArea.getText());
        outputArea.setText("");

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            AgentService service = (AgentService) registry.lookup("AgentService");
            TestCaseService testCaseService = (TestCaseService) registry.lookup("TestCaseService");

            List<TestCase> testCases = testCaseService.getTestCasesByProblemId(currentProblemId);
            if (testCases == null) testCases = new ArrayList<>();

            ScriptTask task = new ScriptTask(codeArea.getText(), currentLang, testCases);

            AgentCallbackImpl callback = new AgentCallbackImpl(
                    outputArea, contestId,
                    currentUser.getId(),
                    currentProblemId,
                    currentLang,
                    codeArea.getText()
            );

            GenericAgent agent = new GenericAgent(task);
            service.submitAgent(agent, callback);

            outputArea.setText("[Info] Code submitted. Running tests...");

        } catch (Exception ex) {
            ex.printStackTrace();
            outputArea.setText("[Error] " + ex.getMessage());
        }
    }

    private void handleClear() {
        codeArea.setText("");
        codeMap.put(currentLang, "");
        outputArea.setText("");
    }

    public void setStarterCode(Map<String, String> starterCode) {
        if (starterCode == null || starterCode.isEmpty()) return;

        codeMap.clear();
        starterCode.forEach((k, v) -> codeMap.put(k.toLowerCase(), v));

        if (codeMap.containsKey(currentLang))
            codeArea.setText(codeMap.get(currentLang));
    }

    public void setCurrentProblemId(int id) {
        this.currentProblemId = id;
    }
}
