package com.rmi.coding.platform.client.compponents;

import com.rmi.coding.platform.agents.GenericAgent;
import com.rmi.coding.platform.agents.tasks.ScriptTask;
import com.rmi.coding.platform.client.AgentCallbackImpl;
import com.rmi.coding.platform.service.AgentService;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

public class MiniScriptIDEPanel extends JPanel {

    private RSyntaxTextArea codeArea;
    private JComboBox<String> langCombo;
    private Map<String, String> codeMap;
    private String currentLang;

    private JButton runButton;
    private JButton clearButton;
    private JTextArea outputArea;

    public MiniScriptIDEPanel() {
        initComponents();
        setupLayout();
        initializeDefaults();
    }

    private void initComponents() {
        codeMap = new HashMap<>();
        currentLang = "python";

        langCombo = new JComboBox<>(new String[]{"Python", "JavaScript"});
        langCombo.setSelectedItem("Python");
        langCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        langCombo.addActionListener(this::handleLanguageChange);

        codeArea = new RSyntaxTextArea();
        codeArea.setCodeFoldingEnabled(true);
        codeArea.setAntiAliasingEnabled(true);
        codeArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        codeArea.setTabSize(4);
        codeArea.setLineWrap(true);
        codeArea.setWrapStyleWord(true);
        codeArea.setAutoIndentEnabled(true);
        codeArea.setCurrentLineHighlightColor(new Color(255, 255, 225));

        runButton = new JButton("Run");
        clearButton = new JButton("Clear");
        outputArea = new JTextArea(10, 80);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(245, 245, 245));

        runButton.addActionListener(e -> handleRun());
        clearButton.addActionListener(e -> handleClear());
    }

    private void setupLayout() {
        setLayout(new BorderLayout(0, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Language: "));
        topPanel.add(langCombo);
        add(topPanel, BorderLayout.NORTH);

        RTextScrollPane codeScroll = new RTextScrollPane(codeArea);
        codeScroll.setLineNumbersEnabled(true);
        add(codeScroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(runButton);
        buttonPanel.add(clearButton);
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);

        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Output / Test Case"));
        bottomPanel.add(outputScroll, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
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

    private void setSyntax(String language) {
        switch (language.toLowerCase()) {
            case "python":
                codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
                break;
            case "javascript":
                codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
                break;
            default:
                codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        }
    }

    private void handleRun() {
        codeMap.put(currentLang, codeArea.getText());
        outputArea.setText("");

        String code = codeArea.getText();
        String lang = currentLang;

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            AgentService service = (AgentService) registry.lookup("AgentService");

            AgentCallbackImpl callback = new AgentCallbackImpl(outputArea);
            ScriptTask task = new ScriptTask(code, lang);
            GenericAgent agent = new GenericAgent(task);

            service.submitAgent(agent, callback);
            outputArea.setText("[Info] Agent submitted to server. Waiting for result...");

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
        starterCode.forEach((lang, code) -> codeMap.put(lang.toLowerCase(), code));

        if (codeMap.containsKey(currentLang)) {
            codeArea.setText(codeMap.get(currentLang));
        } else {
            String firstLang = codeMap.keySet().iterator().next();
            currentLang = firstLang;
            String displayLang = firstLang.substring(0, 1).toUpperCase() + firstLang.substring(1);
            langCombo.setSelectedItem(displayLang);
            codeArea.setText(codeMap.get(firstLang));
            setSyntax(firstLang);
        }
    }
}
