package com.todoapp.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for secure password hashing using BCrypt.
 *
 * Passwords are NEVER stored in plain text.
 * BCrypt automatically generates and embeds a random salt,
 * so two hashes of the same password will be different yet both valid.
 */
public class PasswordUtil {

    /** Work factor – higher = slower (and more secure). 12 is a good default. */
    private static final int BCRYPT_ROUNDS = 12;

    private PasswordUtil() { /* utility class – no instantiation */ }

    /**
     * Hashes a plain-text password with BCrypt.
     *
     * @param plainPassword the raw password entered by the user
     * @return a BCrypt hash safe to store in the database
     */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Verifies a plain-text password against a stored BCrypt hash.
     *
     * @param plainPassword the raw password to check
     * @param hashedPassword the stored hash from the database
     * @return {@code true} if the password matches the hash
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) return false;
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
