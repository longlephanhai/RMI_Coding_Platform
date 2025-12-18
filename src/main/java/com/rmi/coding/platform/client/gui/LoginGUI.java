package com.rmi.coding.platform.client.gui;

import com.rmi.coding.platform.model.User;
import com.rmi.coding.platform.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginGUI extends JPanel {

    private final UserService userService;
    private final ClientGUI parent;

    public LoginGUI(UserService userService, ClientGUI parent) {
        this.userService = userService;
        this.parent = parent;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        // =================== Title ===================
        JLabel title = new JLabel("Welcome Back!", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(60, 60, 60));
        title.setBorder(new EmptyBorder(30, 0, 30, 0));
        add(title, BorderLayout.NORTH);

        // =================== Form Panel ===================
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(20, 30, 20, 30)
        ));
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        // Show password
        gbc.gridx = 1; gbc.gridy = 2;
        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.setBackground(Color.WHITE);
        showPassword.addActionListener(e -> passwordField.setEchoChar(
                showPassword.isSelected() ? (char) 0 : '•'
        ));
        formPanel.add(showPassword, gbc);

        // Login button
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(65, 105, 225));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        loginBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter username and password!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                User user = userService.login(username, password);
                if (user != null) {
                    JOptionPane.showMessageDialog(this, "Login Successful!");
                    parent.onLoginSuccess(user);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Server error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        formPanel.add(loginBtn, gbc);

        // =================== Register link ===================
        gbc.gridy = 4;
        JButton registerLink = new JButton("Don't have an account? Register");
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false);
        registerLink.setForeground(new Color(65, 105, 225));
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        registerLink.addActionListener(e -> parent.showRegisterPanel());
        formPanel.add(registerLink, gbc);

        // Center form panel
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(new Color(245, 245, 245));
        wrapper.add(formPanel);
        add(wrapper, BorderLayout.CENTER);
    }
}
