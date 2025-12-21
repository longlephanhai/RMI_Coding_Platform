package com.rmi.coding.platform.client.compponents;

import com.rmi.coding.platform.agents.GenericAgent;
import com.rmi.coding.platform.agents.tasks.ScriptTask;
import com.rmi.coding.platform.client.callbackImpl.AgentCallbackImpl;
import com.rmi.coding.platform.model.Contest;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

public class MiniScriptIDEContestPanel extends JPanel {

    private RSyntaxTextArea codeArea;
    private JComboBox<String> langCombo;
    private JTextArea outputArea;
    private JButton runButton, clearButton, themeButton;

    private boolean darkMode = false;
    private boolean editorLocked = false;

    private final int contestId;
    private int currentProblemId;
    private final User currentUser;

    private final Map<String, String> codeMap = new HashMap<>();
    private final Map<String, String> starterCodeMap = new HashMap<>();
    private String currentLang = "python";

    private Timer contestStatusTimer;

    public MiniScriptIDEContestPanel(User user, int contestId) {
        this.currentUser = user;
        this.contestId = contestId;

        initComponents();
        setupLayout();
        applyLightTheme();
        startContestStatusWatcher();
    }

    /* ================= UI INIT ================= */

    private void initComponents() {
        langCombo = new JComboBox<>(new String[]{"Python", "JavaScript"});
        langCombo.addActionListener(this::handleLanguageChange);

        themeButton = new JButton("Dark");
        themeButton.addActionListener(e -> toggleTheme());

        codeArea = new RSyntaxTextArea();
        codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
        codeArea.setFont(new Font("Consolas", Font.PLAIN, 15));

        runButton = styledButton("Submit", new Color(46, 204, 113));
        clearButton = styledButton("Reset", new Color(231, 76, 60));

        runButton.addActionListener(e -> handleSubmit());
        clearButton.addActionListener(e -> handleClear());

        outputArea = new JTextArea(8, 80);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 13));
    }

    private JButton styledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    private void setupLayout() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Language:"));
        top.add(langCombo);
        top.add(runButton);
        top.add(clearButton);
        top.add(themeButton);

        add(top, BorderLayout.NORTH);
        add(new RTextScrollPane(codeArea), BorderLayout.CENTER);
        add(new JScrollPane(outputArea), BorderLayout.SOUTH);
    }

    /* ================= REALTIME CONTEST WATCHER ================= */

    private void startContestStatusWatcher() {
        contestStatusTimer = new Timer(1000, e -> {
            try {
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                ContestService contestService =
                        (ContestService) registry.lookup("ContestService");

                Contest contest = contestService.getContestById(contestId);
                LocalDateTime now = LocalDateTime.now();

                if (now.isBefore(contest.getStartTime())) {
                    lockEditor("Contest has not started");
                } else if (now.isAfter(contest.getEndTime())) {
                    lockEditor("Contest has ended");
                } else {
                    unlockEditor();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        contestStatusTimer.start();
    }

    private void lockEditor(String reason) {
        if (editorLocked) return;

        editorLocked = true;
        runButton.setEnabled(false);
        clearButton.setEnabled(false);
        codeArea.setEditable(false);

        outputArea.append("\n[Contest] " + reason + ". Editor locked.\n");
    }

    private void unlockEditor() {
        if (!editorLocked) return;

        editorLocked = false;
        runButton.setEnabled(true);
        clearButton.setEnabled(true);
        codeArea.setEditable(true);
    }

    /* ================= SUBMIT ================= */

    private void handleSubmit() {
        if (editorLocked) return;

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            ContestService contestService =
                    (ContestService) registry.lookup("ContestService");

            ContestParticipantService participantService =
                    (ContestParticipantService) registry.lookup("ContestParticipantService");

            if (!contestService.checkStatusContest(contestId)) {
                JOptionPane.showMessageDialog(this,
                        "Contest is not active",
                        "Submit blocked",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!participantService.isUserJoined(contestId, currentUser.getId())) {
                JOptionPane.showMessageDialog(this,
                        "You must join contest first",
                        "Submit blocked",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            TestCaseService testCaseService =
                    (TestCaseService) registry.lookup("TestCaseService");

            List<TestCase> testCases =
                    testCaseService.getTestCasesByProblemId(currentProblemId);

            ScriptTask task = new ScriptTask(
                    codeArea.getText(),
                    currentLang,
                    testCases
            );

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

            outputArea.setText("[Contest] Submission sent...\n");

        } catch (Exception ex) {
            outputArea.setText("[Error] " + ex.getMessage());
        }
    }

    /* ================= HELPERS ================= */

    private void handleClear() {
        if (editorLocked) return;

        String starter = starterCodeMap.get(currentLang);
        codeArea.setText(starter != null ? starter : "");
    }

    private void handleLanguageChange(ActionEvent e) {
        codeMap.put(currentLang, codeArea.getText());
        currentLang = ((String) langCombo.getSelectedItem()).toLowerCase();
        codeArea.setText(codeMap.getOrDefault(currentLang, ""));
        setSyntax(currentLang);
    }

    private void setSyntax(String lang) {
        codeArea.setSyntaxEditingStyle(
                "python".equals(lang)
                        ? SyntaxConstants.SYNTAX_STYLE_PYTHON
                        : SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT
        );
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

    /* ================= PUBLIC API ================= */

    public void setStarterCode(Map<String, String> starterCode) {
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
