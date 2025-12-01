package com.rmi.coding.platform.client;

import com.rmi.coding.platform.model.User;
import com.rmi.coding.platform.service.ContestService;
import com.rmi.coding.platform.service.ProblemService;
import com.rmi.coding.platform.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

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

        if ("admin".equalsIgnoreCase(user.getRole())) {
            // Nếu là admin, mở AdminGUI
            try {
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                ContestService contestService = (ContestService) registry.lookup("ContestService");
                ProblemService problemService = (ProblemService) registry.lookup("ProblemService");
                new AdminGUI(contestService, problemService); // mở GUI admin
                this.dispose(); // đóng GUI ClientGUI user
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Cannot connect to ContestService", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Nếu là user bình thường, hiển thị panel chính
            showMainPanel();
        }
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
