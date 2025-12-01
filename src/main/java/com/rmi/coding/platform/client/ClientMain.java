package com.rmi.coding.platform.client;

import com.rmi.coding.platform.service.UserService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientMain {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            UserService userService = (UserService) registry.lookup("UserService");

            System.out.println("Connected to RMI server");

            // Tạo GUI chính, truyền UserService
            ClientGUI clientGUI = new ClientGUI(userService);
            clientGUI.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Cannot connect to RMI server!");
        }
    }
}
