package com.barangay.transtrac.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Password handling.
 *
 * DEMO MODE: while STORE_PLAIN_TEXT is true, the password is saved as typed so it is
 * visible in the password_hash column for the proof screenshot (salt is left empty).
 * This is NOT secure. Set it to false to go back to salted PBKDF2 hashes.
 */
public class PasswordUtil {
    private static final boolean STORE_PLAIN_TEXT = true;

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    public String generateSalt() {
        if (STORE_PLAIN_TEXT) {
            return "";
        }
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public String hash(String password, String saltBase64) {
        if (STORE_PLAIN_TEXT) {
            return password;
        }
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return Base64.getEncoder().encodeToString(factory.generateSecret(spec).getEncoded());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Password hashing failed", e);
        }
    }

    public boolean verify(String password, String saltBase64, String expectedHash) {
        return hash(password, saltBase64).equals(expectedHash);
    }
}
