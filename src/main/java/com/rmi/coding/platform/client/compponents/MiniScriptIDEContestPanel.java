package com.rmi.coding.platform.client.compponents;

import com.rmi.coding.platform.agents.GenericAgent;
import com.rmi.coding.platform.agents.tasks.ScriptTask;
import com.rmi.coding.platform.client.callbackImpl.AgentCallbackImpl;
import com.rmi.coding.platform.model.TestCase;
import com.rmi.coding.platform.model.User;
import com.rmi.coding.platform.service.AgentService;
import com.rmi.coding.platform.service.ContestParticipantService;
import com.rmi.coding.platform.service.ContestService;
import com.rmi.coding.platform.service.TestCaseService;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.List;

public class MiniScriptIDEContestPanel extends JPanel {

    private RSyntaxTextArea codeArea;
    private JComboBox<String> langCombo;
    private Map<String, String> codeMap;
    private String currentLang;

    private JButton runButton;
    private JButton clearButton;
    private JButton themeButton;
    private JTextArea outputArea;

    private boolean darkMode = false;

    private int currentProblemId;
    private final int contestId;
    private final User currentUser;

    private final Map<String, String> starterCodeMap = new HashMap<>();

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
        langCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        langCombo.addActionListener(this::handleLanguageChange);

        themeButton = new JButton("Dark");
        themeButton.addActionListener(e -> toggleTheme());

        codeArea = new RSyntaxTextArea();
        codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
        codeArea.setCodeFoldingEnabled(true);
        codeArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        codeArea.setTabSize(4);

        runButton = styledButton("Submit", new Color(46, 204, 113));
        clearButton = styledButton("Reset", new Color(231, 76, 60));

        runButton.addActionListener(e -> handleSubmit());
        clearButton.addActionListener(e -> handleClear());

        outputArea = new JTextArea(10, 80);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 14));
    }

    private JButton styledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        return btn;
    }

    private void setupLayout() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.add(new JLabel("Language:"));
        top.add(langCombo);
        top.add(runButton);
        top.add(clearButton);
        top.add(themeButton);

        add(top, BorderLayout.NORTH);

        add(new RTextScrollPane(codeArea), BorderLayout.CENTER);
        add(new JScrollPane(outputArea), BorderLayout.SOUTH);
    }

    private void initializeDefaults() {
        codeMap.put(currentLang, "");
    }


    private void handleSubmit() {
        runButton.setEnabled(false);
        new Timer(3000, e -> runButton.setEnabled(true)).start();

        codeMap.put(currentLang, codeArea.getText());
        outputArea.setText("");

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            ContestService contestService =
                    (ContestService) registry.lookup("ContestService");

            ContestParticipantService participantService =
                    (ContestParticipantService) registry.lookup("ContestParticipantService");

            if (!contestService.checkStatusContest(contestId)) {
                JOptionPane.showMessageDialog(this,
                        "Contest has ended or not started",
                        "Submit blocked",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!participantService.isUserJoined(contestId, currentUser.getId())) {
                JOptionPane.showMessageDialog(this,
                        "You must join contest before submitting",
                        "Submit blocked",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            TestCaseService testCaseService =
                    (TestCaseService) registry.lookup("TestCaseService");

            List<TestCase> testCases =
                    testCaseService.getTestCasesByProblemId(currentProblemId);

            if (testCases == null) testCases = new ArrayList<>();

            // Create task
            ScriptTask task = new ScriptTask(
                    codeArea.getText(),
                    currentLang,
                    testCases
            );

            // Contest callback
            AgentCallbackImpl callback = new AgentCallbackImpl(
                    outputArea,
                    contestId,
                    currentUser.getId(),
                    currentProblemId,
                    currentLang,
                    codeArea.getText()
            );

            AgentService agentService =
                    (AgentService) registry.lookup("AgentService");

            agentService.submitAgent(new GenericAgent(task), callback);

            outputArea.setText("[Contest] Submission sent...");

        } catch (Exception ex) {
            ex.printStackTrace();
            outputArea.setText("[Error] " + ex.getMessage());
        }
    }

    private void handleClear() {
        String starter = starterCodeMap.get(currentLang);

        if (starter != null) {
            codeArea.setText(starter);
            codeMap.put(currentLang, starter);
        } else {
            codeArea.setText("");
            codeMap.put(currentLang, "");
        }

        outputArea.setText("");
    }

    private void handleLanguageChange(ActionEvent e) {
        String next = ((String) langCombo.getSelectedItem()).toLowerCase();
        codeMap.put(currentLang, codeArea.getText());
        currentLang = next;
        codeArea.setText(codeMap.getOrDefault(currentLang, ""));
        setSyntax(currentLang);
    }

    private void setSyntax(String lang) {
        if ("python".equals(lang))
            codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
        else
            codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
    }

    private void toggleTheme() {
        darkMode = !darkMode;
        if (darkMode) applyDarkTheme();
        else applyLightTheme();
    }

    private void applyDarkTheme() {
        codeArea.setBackground(new Color(30, 30, 30));
        codeArea.setForeground(Color.WHITE);
        outputArea.setBackground(new Color(40, 40, 40));
        outputArea.setForeground(Color.WHITE);
        themeButton.setText("Light");
    }

    private void applyLightTheme() {
        codeArea.setBackground(Color.WHITE);
        codeArea.setForeground(Color.BLACK);
        outputArea.setBackground(new Color(245, 245, 245));
        outputArea.setForeground(Color.BLACK);
        themeButton.setText("Dark");
    }

    public void setStarterCode(Map<String, String> starterCode) {
        if (starterCode == null || starterCode.isEmpty()) return;

        starterCodeMap.clear();
        codeMap.clear();

        starterCode.forEach((k, v) -> {
            starterCodeMap.put(k.toLowerCase(), v);
            codeMap.put(k.toLowerCase(), v);
        });

        codeArea.setText(codeMap.getOrDefault(currentLang, ""));
    }

    public void setCurrentProblemId(int id) {
        this.currentProblemId = id;
    }
}
