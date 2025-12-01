package com.rmi.coding.platform.client;

import com.rmi.coding.platform.model.User;
import com.rmi.coding.platform.service.UserService;

import javax.swing.*;
import java.awt.*;

public class ClientGUI extends JFrame {

    private final UserService userService;
    private User loggedUser;
    private JPanel mainPanel;       // Panel chứa các màn hình
    private CardLayout cardLayout;

    public ClientGUI(UserService userService) {
        this.userService = userService;

        setTitle("Coding Platform");
        setSize(400, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Tạo các panel
        LoginGUI loginGUI = new LoginGUI(this.userService, this);
        RegisterGUI registerGUI = new RegisterGUI(this.userService, this);

        mainPanel.add(loginGUI, "login");
        mainPanel.add(registerGUI, "register");

        add(mainPanel);
        showLoginPanel();
    }

    public void showLoginPanel() {
        cardLayout.show(mainPanel, "login");
    }

    public void showRegisterPanel() {
        cardLayout.show(mainPanel, "register");
    }

    public void onLoginSuccess(User user) {
        this.loggedUser = user;
        showMainPanel();
    }

    private void showMainPanel() {
        getContentPane().removeAll();
        JLabel label = new JLabel("Welcome, " + loggedUser.getUsername() + "! Role: " + loggedUser.getRole(), SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        add(label);
        revalidate();
        repaint();
    }
}
