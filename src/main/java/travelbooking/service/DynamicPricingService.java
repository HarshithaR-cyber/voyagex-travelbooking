package travelbooking.service;

import org.springframework.stereotype.Service;
import travelbooking.dto.PriceFreezeRequest;
import travelbooking.model.PriceFreezeQuote;
import travelbooking.model.PriceHistoryPoint;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DynamicPricingService {

    private final Map<String, PriceFreezeQuote> frozenQuotes = new ConcurrentHashMap<>();

    /**
     * Generates simulated 7-day price history and volatility insight for a route.
     */
    public List<PriceHistoryPoint> getPriceHistory(String origin, String destination, int currentBasePrice) {
        List<PriceHistoryPoint> points = new ArrayList<>();
        LocalDate today = LocalDate.now();

        int base = currentBasePrice > 0 ? currentBasePrice : 200;

        // Generate past 4 days and next 3 days
        int[] deltas = {-25, -15, 0, 10, -5, 20, 35};
        for (int i = 0; i < 7; i++) {
            LocalDate day = today.minusDays(3).plusDays(i);
            int price = Math.max(50, base + deltas[i]);
            String trend = price > base + 15 ? "SURGE" : (price < base - 10 ? "LOW" : "TYPICAL");
            points.add(new PriceHistoryPoint(day.format(DateTimeFormatter.ISO_DATE), price, trend));
        }

        return points;
    }

    /**
     * Calculates dynamic pricing multiplier based on demand, season, and departure proximity.
     */
    public double calculateDynamicDemandMultiplier(LocalDate departureDate) {
        if (departureDate == null) return 1.0;
        long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), departureDate);

        if (daysUntil <= 2) return 1.35; // Last-minute surge
        if (daysUntil <= 7) return 1.18; // Peak window
        if (daysUntil >= 30) return 0.90; // Early bird discount
        return 1.0;
    }

    /**
     * Freezes a price for 48 hours with a refundable deposit.
     */
    public PriceFreezeQuote freezePrice(PriceFreezeRequest req) {
        String token = "PF-" + ThreadLocalRandom.current().nextInt(10000, 99999);
        LocalDateTime expiry = LocalDateTime.now().plusHours(48);
        String expiryStr = expiry.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        int fare = req.safeFare();
        int deposit = Math.max(10, Math.min(30, (int) Math.round(fare * 0.05)));

        PriceFreezeQuote quote = new PriceFreezeQuote(
                token,
                req.flightNumber() != null ? req.flightNumber() : "FLIGHT",
                req.airline() != null ? req.airline() : "Partner Airline",
                req.route() != null ? req.route() : "Route",
                req.departureDate() != null ? req.departureDate() : LocalDate.now().plusDays(7).toString(),
                fare,
                deposit,
                48,
                expiryStr,
                "ACTIVE",
                req.travelerEmail() != null ? req.travelerEmail() : "traveler@example.com"
        );

        frozenQuotes.put(token, quote);
        return quote;
    }

    public Optional<PriceFreezeQuote> getFrozenQuote(String token) {
        if (token == null) return Optional.empty();
        return Optional.ofNullable(frozenQuotes.get(token.trim().toUpperCase()));
    }
}
