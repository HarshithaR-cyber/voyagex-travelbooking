package travelbooking.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travelbooking.dto.*;
import travelbooking.model.UserAccount;

import java.util.*;

@Service
@Transactional
public class AuthService {

    private final UserAccountRepository userRepository;
    private final SessionTokenService sessionTokenService;

    public AuthService(UserAccountRepository userRepository, SessionTokenService sessionTokenService) {
        this.userRepository = userRepository;
        this.sessionTokenService = sessionTokenService;
    }

    @PostConstruct
    public void seedInitialDemoAccounts() {
        if (!userRepository.existsByEmailIgnoreCase("arun.patel@example.com")) {
            String salt1 = PasswordSecurityUtil.generateSalt();
            String hash1 = PasswordSecurityUtil.hashPassword("voyagex123", salt1);
            UserAccount arun = new UserAccount("arun.patel@example.com", "Arun Patel", hash1, salt1, "VoyageX Plus");
            arun.getSavedDealIds().add("deal-1");
            arun.getSavedDealIds().add("deal-3");
            userRepository.save(arun);
        }

        if (!userRepository.existsByEmailIgnoreCase("elena.rostova@example.com")) {
            String salt2 = PasswordSecurityUtil.generateSalt();
            String hash2 = PasswordSecurityUtil.hashPassword("voyagex123", salt2);
            UserAccount elena = new UserAccount("elena.rostova@example.com", "Elena Rostova", hash2, salt2, "VoyageX Elite");
            elena.getSavedDealIds().add("deal-2");
            userRepository.save(elena);
        }
    }

    public AuthResponse register(RegisterRequest req) {
        if (req == null) {
            return new AuthResponse(false, "Registration payload is missing", null, null);
        }

        String email = req.email() != null ? req.email().trim().toLowerCase() : "";
        if (email.isBlank() || !email.contains("@")) {
            return new AuthResponse(false, "Please provide a valid email address", null, null);
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            return new AuthResponse(false, "An account with this email address already exists. Please sign in instead.", null, null);
        }

        if (req.password() == null || req.password().length() < 6) {
            return new AuthResponse(false, "Password must be at least 6 characters in length", null, null);
        }

        if (!req.password().equals(req.confirmPassword())) {
            return new AuthResponse(false, "Passwords do not match", null, null);
        }

        String fullName = req.fullName() != null && !req.fullName().isBlank() ? req.fullName().trim() : "VoyageX Traveler";
        String tier = req.membershipTier() != null && !req.membershipTier().isBlank() ? req.membershipTier() : "VoyageX Explorer";

        String salt = PasswordSecurityUtil.generateSalt();
        String hash = PasswordSecurityUtil.hashPassword(req.password(), salt);

        UserAccount user = new UserAccount(email, fullName, hash, salt, tier);
        UserAccount saved = userRepository.save(user);

        String token = "vx-auth-" + UUID.randomUUID();
        sessionTokenService.registerToken(token, email);
        return new AuthResponse(true, "Account created successfully! Welcome to VoyageX.", token, toDto(saved));
    }

    public AuthResponse login(LoginRequest req) {
        if (req == null) {
            return new AuthResponse(false, "Login credentials missing", null, null);
        }

        String email = req.email() != null ? req.email().trim().toLowerCase() : "";
        Optional<UserAccount> userOpt = userRepository.findByEmailIgnoreCase(email);

        if (userOpt.isEmpty()) {
            return new AuthResponse(false, "Invalid email or password", null, null);
        }

        UserAccount user = userOpt.get();
        boolean valid = PasswordSecurityUtil.verifyPassword(req.password(), user.getSalt(), user.getPasswordHash());

        if (!valid) {
            return new AuthResponse(false, "Invalid email or password", null, null);
        }

        String token = "vx-auth-" + UUID.randomUUID();
        sessionTokenService.registerToken(token, email);
        return new AuthResponse(true, "Signed in successfully! Welcome back, " + user.getFullName() + ".", token, toDto(user));
    }

    public Map<String, Object> forgotPassword(ForgotPasswordRequest req) {
        String email = req != null && req.email() != null ? req.email().trim().toLowerCase() : "";
        boolean exists = userRepository.existsByEmailIgnoreCase(email);

        return Map.of(
                "success", true,
                "email", email,
                "accountFound", exists,
                "message", exists
                        ? "A secure password reset link has been dispatched to " + email + ". Please check your inbox within 15 minutes."
                        : "If an account with this email address exists in VoyageX, password reset instructions have been sent."
        );
    }

    public Optional<UserDto> getUserByEmail(String email) {
        if (email == null) return Optional.empty();
        return userRepository.findByEmailIgnoreCase(email.trim().toLowerCase()).map(this::toDto);
    }

    public Set<String> toggleSavedDeal(String email, String dealId) {
        if (email == null || dealId == null) return Set.of();
        Optional<UserAccount> userOpt = userRepository.findByEmailIgnoreCase(email.trim().toLowerCase());
        if (userOpt.isEmpty()) return Set.of();

        UserAccount user = userOpt.get();
        Set<String> saved = user.getSavedDealIds();
        if (saved.contains(dealId)) {
            saved.remove(dealId);
        } else {
            saved.add(dealId);
        }
        userRepository.save(user);
        return saved;
    }

    public Set<String> getSavedDeals(String email) {
        if (email == null) return Set.of();
        return userRepository.findByEmailIgnoreCase(email.trim().toLowerCase())
                .map(UserAccount::getSavedDealIds)
                .orElse(Set.of());
    }

    public Optional<UserDto> updateTier(String email, String tier) {
        if (email == null || tier == null) return Optional.empty();
        return userRepository.findByEmailIgnoreCase(email.trim().toLowerCase())
                .map(user -> {
                    user.setMembershipTier(tier);
                    return toDto(userRepository.save(user));
                });
    }

    public void registerDemoToken(String token, String email) {
        sessionTokenService.registerToken(token, email);
    }

    public void revokeToken(String token) {
        sessionTokenService.revokeToken(token);
    }

    public Optional<UserDto> verifySession(String token) {
        return sessionTokenService.verifyToken(token)
                .flatMap(email -> userRepository.findByEmailIgnoreCase(email).map(this::toDto));
    }

    private UserDto toDto(UserAccount u) {
        return new UserDto(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getMembershipTier(),
                u.getInitials(),
                new HashSet<>(u.getSavedDealIds()),
                u.getCreatedDate()
        );
    }
}
