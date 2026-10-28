package travelbooking.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import travelbooking.dto.*;
import travelbooking.service.AuthService;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthApiController {

    private final AuthService authService;

    public AuthApiController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse resp = authService.register(req);
        if (!resp.success()) {
            return ResponseEntity.badRequest().body(resp);
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse resp = authService.login(req);
        if (!resp.success()) {
            return ResponseEntity.status(401).body(resp);
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        return ResponseEntity.ok(authService.forgotPassword(req));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestParam String email) {
        return authService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @PostMapping("/demo-login")
    public ResponseEntity<Map<String, Object>> demoLogin(@RequestBody(required = false) Map<String, String> body) {
        String username = (body != null && body.containsKey("username")) ? body.get("username").toLowerCase() : "arun";
        String email = username.contains("elena") ? "elena.rostova@example.com" : "arun.patel@example.com";
        String token = "demo-jwt-" + System.currentTimeMillis();

        return authService.getUserByEmail(email)
                .map(u -> {
                    authService.registerDemoToken(token, u.email());
                    return ResponseEntity.ok(Map.<String, Object>of(
                            "success", true,
                            "name", u.fullName(),
                            "email", u.email(),
                            "tier", u.membershipTier(),
                            "initials", u.initials(),
                            "savedDeals", u.savedDealIds(),
                            "token", token
                    ));
                })
                .orElseGet(() -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "name", "Arun Patel",
                        "email", "arun.patel@example.com",
                        "tier", "Voyager VIP Gold",
                        "initials", "AP",
                        "savedDeals", Set.of("deal-1"),
                        "token", token
                )));
    }

    @PostMapping("/deals/save")
    public ResponseEntity<Map<String, Object>> toggleSaveDeal(@RequestBody Map<String, String> body) {
        String email = body != null ? body.get("email") : null;
        String dealId = body != null ? body.get("dealId") : null;

        if (email == null || dealId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email and dealId are required"));
        }

        Set<String> updated = authService.toggleSavedDeal(email, dealId);
        boolean isSaved = updated.contains(dealId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "dealId", dealId,
                "saved", isSaved,
                "savedCount", updated.size(),
                "message", isSaved ? "Deal saved to your VoyageX profile!" : "Deal removed from saved list."
        ));
    }

    @GetMapping("/deals/saved")
    public ResponseEntity<Set<String>> getSavedDeals(@RequestParam String email) {
        return ResponseEntity.ok(authService.getSavedDeals(email));
    }

    @GetMapping("/verify-session")
    public ResponseEntity<?> verifySession(@RequestParam(required = false) String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("valid", false, "message", "No session token provided"));
        }
        return authService.verifySession(token)
                .map(u -> ResponseEntity.ok(Map.<String, Object>of("valid", true, "user", u)))
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("valid", false, "message", "Session expired or invalid")));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestBody(required = false) Map<String, String> body) {
        String token = body != null ? body.get("token") : null;
        if (token != null) authService.revokeToken(token);
        return ResponseEntity.ok(Map.of("success", true, "message", "Signed out successfully."));
    }

    @PostMapping("/membership/upgrade")
    public ResponseEntity<?> upgradeMembership(@RequestBody Map<String, String> body) {
        String email = body != null ? body.get("email") : null;
        String tier = body != null ? body.get("tier") : "VoyageX Plus";

        if (email == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email is required"));
        }

        return authService.updateTier(email, tier)
                .map(u -> ResponseEntity.ok(Map.<String, Object>of(
                        "success", true,
                        "message", "Membership upgraded to " + tier + " (Academic Simulation)",
                        "tier", u.membershipTier()
                )))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("success", false, "message", "User not found")));
    }
}
