package travelbooking.service;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory session token registry for VoyageX authentication.
 * Stores issued auth tokens with their associated user email and expiry.
 * Tokens expire after 8 hours of inactivity; refreshed on each valid use.
 */
@Service
public class SessionTokenService {

    private static final long TOKEN_TTL_MS = 8 * 60 * 60 * 1000L; // 8 hours

    private record TokenEntry(String email, Instant expiresAt) {}

    private final Map<String, TokenEntry> tokenStore = new ConcurrentHashMap<>();

    /**
     * Register a new token for the given email (called on login/register).
     */
    public void registerToken(String token, String email) {
        if (token == null || email == null) return;
        tokenStore.put(token, new TokenEntry(email.trim().toLowerCase(), Instant.now().plusMillis(TOKEN_TTL_MS)));
    }

    /**
     * Verify a token. Returns the associated email if valid, empty if expired or unknown.
     * Also refreshes TTL on success.
     */
    public Optional<String> verifyToken(String token) {
        if (token == null) return Optional.empty();
        TokenEntry entry = tokenStore.get(token);
        if (entry == null) return Optional.empty();
        if (Instant.now().isAfter(entry.expiresAt())) {
            tokenStore.remove(token);
            return Optional.empty();
        }
        // Refresh TTL on valid use
        tokenStore.put(token, new TokenEntry(entry.email(), Instant.now().plusMillis(TOKEN_TTL_MS)));
        return Optional.of(entry.email());
    }

    /**
     * Invalidate a token (on logout).
     */
    public void revokeToken(String token) {
        if (token != null) tokenStore.remove(token);
    }

    /**
     * Purge expired tokens (can be called periodically).
     */
    public void purgeExpired() {
        Instant now = Instant.now();
        tokenStore.entrySet().removeIf(e -> now.isAfter(e.getValue().expiresAt()));
    }
}
