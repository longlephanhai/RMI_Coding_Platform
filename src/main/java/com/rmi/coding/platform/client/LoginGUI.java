package com.rmi.coding.platform.client;

import com.rmi.coding.platform.model.User;
import com.rmi.coding.platform.service.UserService;

import javax.swing.*;
import java.awt.*;

public class LoginGUI extends JPanel {

    private final UserService userService;
    private final ClientGUI parent;

    public LoginGUI(UserService userService, ClientGUI parent) {
        this.userService = userService;
        this.parent = parent;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        // Title
        JLabel title = new JLabel("Login", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        formPanel.setBackground(new Color(245, 245, 245));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(100, 149, 237));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);

        loginBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

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
                JOptionPane.showMessageDialog(this, "Server error!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        formPanel.add(loginBtn);

        // Register link
        JButton registerLink = new JButton("Don't have an account? Register");
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false);
        registerLink.setForeground(Color.BLUE);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.addActionListener(e -> parent.showRegisterPanel());

        add(formPanel, BorderLayout.CENTER);
        add(registerLink, BorderLayout.SOUTH);
    }
}
