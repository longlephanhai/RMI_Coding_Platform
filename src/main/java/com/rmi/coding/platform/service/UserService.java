package com.rmi.coding.platform.service;

import com.rmi.coding.platform.model.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UserService extends Remote {
    boolean register(String username, String password, String email) throws RemoteException;

    User login(String username, String password) throws RemoteException;

    User getUserById(int userId) throws RemoteException;
}
