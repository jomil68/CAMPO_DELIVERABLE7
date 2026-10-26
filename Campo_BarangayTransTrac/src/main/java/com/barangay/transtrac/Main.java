package com.barangay.transtrac;

import com.barangay.transtrac.dao.DatabaseInitializer;
import com.barangay.transtrac.dao.UserDao;
import com.barangay.transtrac.ui.Authentication;
import com.barangay.transtrac.util.PasswordUtil;

import java.util.Scanner;

/** Single entry point: Initialize DB -> instantiate DAOs/utilities -> Authentication.start(). */
public class Main {
    public static void main(String[] args) {
        // 1. Initialize database
        DatabaseInitializer.initialize();

        // 2. Instantiate shared DAOs and utilities
        UserDao userDao = new UserDao();
        PasswordUtil passwordUtil = new PasswordUtil();
        Scanner scanner = new Scanner(System.in);

        // 3. Delegate to Authentication
        Authentication authentication = new Authentication(userDao, scanner, passwordUtil);
        authentication.start();

        scanner.close();
    }
}
