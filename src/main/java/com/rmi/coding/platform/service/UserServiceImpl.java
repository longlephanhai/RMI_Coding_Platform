package com.rmi.coding.platform.service;

import com.rmi.coding.platform.config.DatabaseConnection;
import com.rmi.coding.platform.model.User;

import com.rmi.coding.platform.repository.UserRepository;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.sql.Connection;

public class UserServiceImpl extends UnicastRemoteObject implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl() throws RemoteException {
        super();
        try {
            Connection conn = DatabaseConnection.getConnection();
            userRepository = new UserRepository(conn);
        } catch (Exception e) {
            throw new RemoteException("DB Connection error", e);
        }
    }

    @Override
    public boolean register(String username, String password, String email) throws RemoteException {
        try {
            if (userRepository.usernameExists(username)) {
                return false;
            }
            String hash = hashPassword(password);
            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(hash);
            user.setEmail(email);
            user.setRole("user");
            user.setCreatedAt(java.time.LocalDateTime.now());
            return userRepository.register(user);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public User login(String username, String password) throws RemoteException {
        try {
            String hash = hashPassword(password);
            return userRepository.login(username, hash);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
