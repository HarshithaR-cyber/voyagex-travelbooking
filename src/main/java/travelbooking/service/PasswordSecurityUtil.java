package travelbooking.service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Standard PBKDF2 password hashing utility with cryptographic salt.
 * Ensures passwords are never stored in plaintext and verifies in constant time.
 */
public final class PasswordSecurityUtil {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordSecurityUtil() {}

    /**
     * Generates a random 16-byte cryptographic salt.
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a plaintext password with the provided salt using PBKDF2WithHmacSHA256.
     */
    public static String hashPassword(String password, String salt) {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("Password and salt must not be null");
        }
        try {
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Cryptographic hashing algorithm unavailable", e);
        }
    }

    /**
     * Verifies a candidate plaintext password against the stored salt and hash in constant time.
     */
    public static boolean verifyPassword(String candidatePassword, String salt, String expectedHash) {
        if (candidatePassword == null || salt == null || expectedHash == null) {
            return false;
        }
        String candidateHash = hashPassword(candidatePassword, salt);
        return constantTimeEquals(candidateHash, expectedHash);
    }

    private static boolean constantTimeEquals(String a, String b) {
        byte[] aBytes = a.getBytes();
        byte[] bBytes = b.getBytes();
        if (aBytes.length != bBytes.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        return result == 0;
    }
}
