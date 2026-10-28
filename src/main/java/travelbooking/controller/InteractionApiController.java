package travelbooking.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import travelbooking.dto.NewsletterSubscriptionRequest;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class InteractionApiController {

    private final Set<String> subscribedEmails = Collections.synchronizedSet(new HashSet<>());

    @PostMapping("/newsletter/subscribe")
    public ResponseEntity<Map<String, Object>> subscribeNewsletter(@Valid @RequestBody NewsletterSubscriptionRequest req) {
        String email = req.email().trim().toLowerCase();
        subscribedEmails.add(email);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "email", email,
                "message", "Welcome to VoyageX Insider! Secret fares and promo codes will be sent to " + email
        ));
    }


    @GetMapping("/trips/my-trips")
    public ResponseEntity<List<Map<String, Object>>> getMyTrips() {
        return ResponseEntity.ok(List.of(
                Map.of(
                        "bookingReference", "VX-71934",
                        "type", "FLIGHT",
                        "title", "Air India (AI 101) - DEL → BOM",
                        "departureDate", LocalDate.now().plusDays(4).toString(),
                        "departureTime", "06:15 AM",
                        "passengers", 2,
                        "status", "CONFIRMED",
                        "totalPaid", 224,
                        "seat", "12A, 12B"
                ),
                Map.of(
                        "bookingReference", "HT-82910",
                        "type", "HOTEL",
                        "title", "Astra Caldera Luxury Suites - Santorini",
                        "departureDate", LocalDate.now().plusDays(15).toString(),
                        "departureTime", "Check-in 03:00 PM",
                        "passengers", 2,
                        "status", "CONFIRMED",
                        "totalPaid", 640,
                        "seat", "Deluxe Suite"
                )
        ));
    }
}
