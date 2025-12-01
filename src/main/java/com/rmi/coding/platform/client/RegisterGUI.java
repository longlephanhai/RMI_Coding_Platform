package com.rmi.coding.platform.client;

import com.rmi.coding.platform.service.UserService;

import javax.swing.*;
import java.awt.*;

public class RegisterGUI extends JPanel {

    private final UserService userService;
    private final ClientGUI parent;

    public RegisterGUI(UserService userService, ClientGUI parent) {
        this.userService = userService;
        this.parent = parent;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        // Title
        JLabel title = new JLabel("Register", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridLayout(8, 1, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        formPanel.setBackground(new Color(245, 245, 245));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();
        JTextField emailField = new JTextField();

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Confirm Password:"));
        formPanel.add(confirmPasswordField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);

        // Register button
        JButton registerBtn = new JButton("Register");
        registerBtn.setBackground(new Color(60, 179, 113));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFocusPainted(false);

        registerBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String email = emailField.getText().trim();

            // Validation
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
                JOptionPane.showMessageDialog(this, "Invalid email format!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Call RMI service
            try {
                boolean success = userService.register(username, password, email);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Register successful! Please login.");
                    parent.showLoginPanel();
                } else {
                    JOptionPane.showMessageDialog(this, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Server error!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Login link
        JButton loginLink = new JButton("Already have an account? Login");
        loginLink.setBorderPainted(false);
        loginLink.setContentAreaFilled(false);
        loginLink.setForeground(Color.BLUE);
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.addActionListener(e -> parent.showLoginPanel());

        // Layout
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(245, 245, 245));
        bottomPanel.add(registerBtn, BorderLayout.CENTER);
        bottomPanel.add(loginLink, BorderLayout.SOUTH);

        add(formPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
