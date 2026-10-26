package com.barangay.transtrac.ui;

import com.barangay.transtrac.dao.UserDao;
import com.barangay.transtrac.model.Role;
import com.barangay.transtrac.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void show(User current) {
        while (true) {
            System.out.println("\n===== ADMINISTRATOR MENU =====");
            System.out.println("Logged in as: " + current.getFullName());
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role & Status");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "4" -> updateUser(current);
                case "0" -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void viewAllUsers() {
        System.out.println("\n--- ALL SYSTEM USERS ---");
        List<User> users = userDao.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        printUserTable(users);
    }

    private void viewPendingUsers() {
        System.out.println("\n--- PENDING REGISTRATIONS ---");
        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("No pending registrations.");
            return;
        }
        printUserTable(pending);
    }

    private void approveUser() {
        System.out.println("\n--- APPROVE USER ACCOUNT ---");
        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("No pending registrations to approve.");
            return;
        }
        printUserTable(pending);

        while (true) {
            System.out.print("Enter Target User ID to approve (0 to cancel): ");
            String input = scanner.nextLine().trim();

            int id;
            try {
                id = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            if (id == 0) {
                System.out.println("Approval cancelled.");
                return;
            }

            User target = null;
            for (User u : pending) {
                if (u.getUserId() == id) {
                    target = u;
                    break;
                }
            }
            if (target == null) {
                System.out.println("User ID " + id + " is not in the pending list. Try again.");
                continue;
            }

            if (userDao.approveUser(id)) {
                System.out.println("SUCCESS: User ID " + id + " (" + target.getUsername()
                        + ") has been APPROVED.");
            } else {
                System.out.println("Approval failed. Please try again.");
            }
            return;
        }
    }

    /** Lets an administrator change another user's role and status. Enter 0 at any step to cancel. */
    private void updateUser(User currentAdmin) {
        System.out.println("\n--- UPDATE USER ROLE & STATUS ---");
        List<User> users = userDao.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        printUserTable(users);

        while (true) {
            int targetUserId = readIntInput("Enter Target User ID to update (0 to cancel): ");
            if (targetUserId == 0) {
                System.out.println("Update cancelled.");
                return;
            }

            if (targetUserId == currentAdmin.getUserId()) {
                System.out.println("WARNING: Modifying the currently active admin account is restricted.");
                continue;
            }

            User target = null;
            for (User u : users) {
                if (u.getUserId() == targetUserId) {
                    target = u;
                    break;
                }
            }
            if (target == null) {
                System.out.println("User ID " + targetUserId + " does not exist. Try again.");
                continue;
            }

            System.out.println("Selected: " + target.getUsername()
                    + " (Role: " + target.getRole().name() + ", Status: " + target.getStatus() + ")");

            String newRole = readValidRole();
            if (newRole == null) {
                System.out.println("Update cancelled.");
                return;
            }

            String newStatus = readValidStatus();
            if (newStatus == null) {
                System.out.println("Update cancelled.");
                return;
            }

            if (userDao.updateUserRoleAndStatus(targetUserId, newRole, newStatus)) {
                System.out.println("SUCCESS: User ID " + targetUserId + " (" + target.getUsername()
                        + ") updated -> Role: " + newRole + ", Status: " + newStatus);
            } else {
                System.out.println("Update failed. Please try again.");
            }
            return;
        }
    }

    /** Keeps asking until the user types a whole number. Letters/symbols never crash the app. */
    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    /** Keeps asking until a valid role is entered. Returns the role name, or null if 0 (cancel) is entered. */
    private String readValidRole() {
        StringBuilder valid = new StringBuilder();
        for (Role r : Role.values()) {
            if (valid.length() > 0) valid.append(", ");
            valid.append(r.name());
        }
        while (true) {
            System.out.print("Enter new role (" + valid + ") or 0 to cancel: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("0")) {
                return null;
            }
            for (Role r : Role.values()) {
                if (r.name().equals(input)) {
                    return r.name();
                }
            }
            System.out.println("Invalid role. Valid roles: " + valid);
        }
    }

    /** Keeps asking until APPROVED or PENDING is entered. Returns null if 0 (cancel) is entered. */
    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new status (APPROVED, PENDING) or 0 to cancel: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("0")) {
                return null;
            }
            if (input.equals("APPROVED") || input.equals("PENDING")) {
                return input;
            }
            System.out.println("Invalid status. Valid statuses: APPROVED, PENDING");
        }
    }

    /** Prints any list of users as an aligned, bordered table. */
    private void printUserTable(List<User> users) {
        String border = "+" + "-".repeat(7) + "+" + "-".repeat(17) + "+"
                      + "-".repeat(16) + "+" + "-".repeat(12) + "+";
        System.out.println(border);
        System.out.println(String.format("| %-5s | %-15s | %-14s | %-10s |",
                "ID", "USERNAME", "ROLE", "STATUS"));
        System.out.println(border);
        for (User u : users) {
            System.out.println(String.format("| %-5s | %-15s | %-14s | %-10s |",
                    u.getUserId(), u.getUsername(), u.getRole().name(), u.getStatus()));
        }
        System.out.println(border);
        System.out.println("Total: " + users.size());
    }
}
