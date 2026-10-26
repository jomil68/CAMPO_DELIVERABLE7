package com.barangay.transtrac.ui;

import com.barangay.transtrac.dao.UserDao;
import com.barangay.transtrac.model.Role;
import com.barangay.transtrac.model.User;
import com.barangay.transtrac.util.PasswordUtil;

import java.util.Scanner;

/** Handles login, registration and role-based routing. Uses one shared UserDao and Scanner. */
public class Authentication {
    private final UserDao userDao;
    private final Scanner scanner;
    private final PasswordUtil passwordUtil;

    private final AdminMenu adminMenu;
    private final StaffMenu staffMenu;
    private final CaptainMenu captainMenu;
    private final TreasurerMenu treasurerMenu;

    public Authentication(UserDao userDao, Scanner scanner, PasswordUtil passwordUtil) {
        this.userDao = userDao;
        this.scanner = scanner;
        this.passwordUtil = passwordUtil;
        this.adminMenu = new AdminMenu(userDao, scanner);
        this.staffMenu = new StaffMenu(userDao, scanner);
        this.captainMenu = new CaptainMenu(userDao, scanner);
        this.treasurerMenu = new TreasurerMenu(userDao, scanner);
    }

    public void start() {
        while (true) {
            System.out.println("\n=====================================");
            System.out.println("   BARANGAY TRANSTRAC");
            System.out.println("=====================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> login();
                case "2" -> register();
                case "0" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void register() {
        System.out.println("\n--- REGISTER ---");
        System.out.print("Full name: ");
        String fullName = scanner.nextLine().trim();
        if (fullName.isEmpty()) {
            System.out.println("Full name is required.");
            return;
        }

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        if (username.length() < 4) {
            System.out.println("Username must be at least 4 characters.");
            return;
        }
        if (userDao.usernameExists(username)) {
            System.out.println("Username already taken.");
            return;
        }

        System.out.print("Password (min 6 characters): ");
        String password = scanner.nextLine();
        if (password.length() < 6) {
            System.out.println("Password must be at least 6 characters.");
            return;
        }

        Role role = chooseRole();
        if (role == null) {
            System.out.println("Registration cancelled.");
            return;
        }

        String salt = passwordUtil.generateSalt();
        String hash = passwordUtil.hash(password, salt);
        User user = new User(username, hash, salt, fullName, role);   // status = PENDING

        if (userDao.register(user)) {
            System.out.println("Registration successful! Account status: PENDING.");
            System.out.println("An administrator must approve your account before you can log in.");
        }
    }

    private Role chooseRole() {
        System.out.println("Select role:");
        Role[] roles = Role.values();
        for (int i = 0; i < roles.length; i++) {
            System.out.printf("%d. %s%n", i + 1, roles[i].getLabel());
        }
        System.out.println("0. Cancel");
        System.out.print("Choice: ");
        String input = scanner.nextLine().trim();
        try {
            int n = Integer.parseInt(input);
            if (n >= 1 && n <= roles.length) {
                return roles[n - 1];
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private void login() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = userDao.findByUsername(username);
        if (user == null || !passwordUtil.verify(password, user.getSalt(), user.getPasswordHash())) {
            System.out.println("Invalid username or password.");
            return;
        }
        if (!user.isActive()) {
            System.out.println("This account has been deactivated.");
            return;
        }
        if (user.getStatus().equals("PENDING")) {
            System.out.println("Your account is still PENDING approval.");
            return;
        }
        if (user.getStatus().equals("REJECTED")) {
            System.out.println("Your registration was rejected. Contact the administrator.");
            return;
        }

        System.out.println("Welcome, " + user.getFullName() + "!");
        routeToMenu(user);
    }

    private void routeToMenu(User user) {
        switch (user.getRole()) {
            case ADMINISTRATOR -> adminMenu.show(user);
            case STAFF -> staffMenu.show(user);
            case CAPTAIN -> captainMenu.show(user);
            case TREASURER -> treasurerMenu.show(user);
        }
    }
}
