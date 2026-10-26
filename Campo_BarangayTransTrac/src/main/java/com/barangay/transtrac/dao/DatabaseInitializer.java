package com.barangay.transtrac.dao;

import com.barangay.transtrac.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Creates all required tables (parents first) before the CLI menu starts. */
public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS residents (
                    resident_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    first_name  TEXT NOT NULL,
                    middle_name TEXT,
                    last_name   TEXT NOT NULL,
                    birthdate   TEXT NOT NULL,
                    sex         TEXT NOT NULL,
                    civil_status TEXT NOT NULL,
                    purok       TEXT,
                    address     TEXT,
                    contact_no  TEXT,
                    is_active   INTEGER NOT NULL DEFAULT 1,
                    created_at  TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transaction_types (
                    type_id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    type_name         TEXT NOT NULL UNIQUE,
                    description       TEXT,
                    fee_amount        REAL NOT NULL DEFAULT 0,
                    requires_approval INTEGER NOT NULL DEFAULT 0,
                    is_active         INTEGER NOT NULL DEFAULT 1
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    username      TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    salt          TEXT NOT NULL,
                    full_name     TEXT NOT NULL,
                    role          TEXT NOT NULL CHECK (role IN ('ADMINISTRATOR','STAFF','CAPTAIN','TREASURER')),
                    status        TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED','REJECTED')),
                    is_active     INTEGER NOT NULL DEFAULT 1,
                    created_at    TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    reference_no   TEXT NOT NULL UNIQUE,
                    resident_id    INTEGER NOT NULL,
                    type_id        INTEGER NOT NULL,
                    processed_by   INTEGER,
                    status         TEXT NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING','PROCESSING','APPROVED','RELEASED','REJECTED')),
                    purpose        TEXT,
                    date_filed     TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    date_released  TEXT,
                    is_voided      INTEGER NOT NULL DEFAULT 0,
                    void_reason    TEXT,
                    FOREIGN KEY (resident_id)  REFERENCES residents(resident_id),
                    FOREIGN KEY (type_id)      REFERENCES transaction_types(type_id),
                    FOREIGN KEY (processed_by) REFERENCES users(user_id)
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS payments (
                    payment_id     INTEGER PRIMARY KEY AUTOINCREMENT,
                    transaction_id INTEGER NOT NULL,
                    amount_paid    REAL NOT NULL,
                    or_number      TEXT NOT NULL UNIQUE,
                    payment_date   TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    received_by    INTEGER NOT NULL,
                    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id),
                    FOREIGN KEY (received_by)    REFERENCES users(user_id)
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS audit_logs (
                    log_id        INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id       INTEGER,
                    action        TEXT NOT NULL,
                    table_name    TEXT,
                    record_id     INTEGER,
                    old_value     TEXT,
                    new_value     TEXT,
                    log_timestamp TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(user_id)
                )""");

            seedDefaultAdmin(conn);
            System.out.println("[DB] Database initialized (foreign keys ON).");

        } catch (SQLException e) {
            throw new IllegalStateException("Database initialization failed: " + e.getMessage(), e);
        }
    }

    /** Creates a first APPROVED administrator so there is someone who can approve PENDING accounts. */
    private static void seedDefaultAdmin(Connection conn) throws SQLException {
        try (Statement check = conn.createStatement();
             ResultSet rs = check.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }
        PasswordUtil util = new PasswordUtil();
        String salt = util.generateSalt();
        String hash = util.hash("admin123", salt);
        String sql = "INSERT INTO users (username, password_hash, salt, full_name, role, status) "
                   + "VALUES (?, ?, ?, ?, 'ADMINISTRATOR', 'APPROVED')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "admin");
            ps.setString(2, hash);
            ps.setString(3, salt);
            ps.setString(4, "System Administrator");
            ps.executeUpdate();
        }
        System.out.println("[DB] Default admin created -> username: admin, password: admin123");
    }
}
