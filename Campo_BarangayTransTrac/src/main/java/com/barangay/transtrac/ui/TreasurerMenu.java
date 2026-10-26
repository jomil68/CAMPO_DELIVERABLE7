package com.barangay.transtrac.ui;

import com.barangay.transtrac.dao.UserDao;
import com.barangay.transtrac.model.User;

import java.util.Scanner;

public class TreasurerMenu {
    private final UserDao userDao;
    private final Scanner scanner;

    public TreasurerMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void show(User current) {
        while (true) {
            System.out.println("\n===== TREASURER MENU =====");
            System.out.println("Logged in as: " + current.getFullName());
            System.out.println("1. Record payments   (coming in next deliverable)");
            System.out.println("2. Collection and fee reports   (coming in next deliverable)");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            if (choice.equals("0")) {
                return;
            } else if (choice.matches("[1-2]")) {
                System.out.println("Coming soon.");
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }
}
