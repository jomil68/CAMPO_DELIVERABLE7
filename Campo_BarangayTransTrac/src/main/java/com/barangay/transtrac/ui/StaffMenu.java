package com.barangay.transtrac.ui;

import com.barangay.transtrac.dao.UserDao;
import com.barangay.transtrac.model.User;

import java.util.Scanner;

public class StaffMenu {
    private final UserDao userDao;
    private final Scanner scanner;

    public StaffMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void show(User current) {
        while (true) {
            System.out.println("\n===== STAFF / CLERK MENU =====");
            System.out.println("Logged in as: " + current.getFullName());
            System.out.println("1. Resident records   (coming in next deliverable)");
            System.out.println("2. File and process transactions   (coming in next deliverable)");
            System.out.println("3. Update request status   (coming in next deliverable)");
            System.out.println("4. Print certificates   (coming in next deliverable)");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            if (choice.equals("0")) {
                return;
            } else if (choice.matches("[1-4]")) {
                System.out.println("Coming soon.");
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }
}
