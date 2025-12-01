package com.rmi.coding.platform.client.compponents;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;

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

        // Language selector combo box
        langCombo = new JComboBox<>(new String[]{"Python", "JavaScript"});
        langCombo.setSelectedItem("Python");
        langCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        langCombo.addActionListener(this::handleLanguageChange);

        // Code editor area
        codeArea = new RSyntaxTextArea();
        codeArea.setCodeFoldingEnabled(true);
        codeArea.setAntiAliasingEnabled(true);
        codeArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        codeArea.setTabSize(4);
        codeArea.setLineWrap(true);
        codeArea.setWrapStyleWord(true);
        codeArea.setAutoIndentEnabled(true);
        codeArea.setCurrentLineHighlightColor(new Color(255, 255, 225));

        // Buttons
        runButton = new JButton("Run");
        clearButton = new JButton("Clear");

        runButton.addActionListener(e -> handleRun());
        clearButton.addActionListener(e -> handleClear());

        // Output area
        outputArea = new JTextArea(10, 80);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(245, 245, 245));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(0, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel: language selector
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Language: "));
        topPanel.add(langCombo);
        add(topPanel, BorderLayout.NORTH);

        // Center panel: code editor
        RTextScrollPane codeScroll = new RTextScrollPane(codeArea);
        codeScroll.setLineNumbersEnabled(true);
        add(codeScroll, BorderLayout.CENTER);

        // Bottom panel: buttons + output
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

        // Save current code
        codeMap.put(currentLang, codeArea.getText());

        // Switch language
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

    // Handle Run button click
    private void handleRun() {
        codeMap.put(currentLang, codeArea.getText());
        outputArea.setText(""); // clear previous output

        // Demo: hiển thị code hiện tại và ngôn ngữ
        outputArea.append("Language: " + currentLang + "\n");
        outputArea.append("Code:\n");
        outputArea.append(codeArea.getText());
        outputArea.append("\n\n[Note] Actual code execution to be implemented on server side.");
    }

    // Handle Clear button click
    private void handleClear() {
        codeArea.setText("");
        codeMap.put(currentLang, "");
        outputArea.setText("");
    }

    public String getCurrentLang() {
        return currentLang;
    }

    public RSyntaxTextArea getCodeArea() {
        return codeArea;
    }

    public JTextArea getOutputArea() {
        return outputArea;
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
            String displayLang = firstLang.substring(0,1).toUpperCase() + firstLang.substring(1);
            langCombo.setSelectedItem(displayLang);
            codeArea.setText(codeMap.get(firstLang));
            setSyntax(firstLang);
        }
    }

    public Map<String, String> getCodeMap() {
        codeMap.put(currentLang, codeArea.getText());
        return new HashMap<>(codeMap);
    }

}
