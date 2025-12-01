package com.rmi.coding.platform.server;

import com.rmi.coding.platform.service.UserService;
import com.rmi.coding.platform.service.UserServiceImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        try {
            // Khởi tạo registry
            Registry registry = LocateRegistry.createRegistry(1099);

            // Tạo service
            UserService userService = new UserServiceImpl();

            // Bind service
            registry.rebind("UserService", userService);

            System.out.println("RMI Server is running on port 1099...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
