package travelbooking.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Locked-in price quote for the 48-hour Price Freeze feature.
 */
public record PriceFreezeQuote(
        String freezeToken,
        String flightNumber,
        String airline,
        String route,
        String departureDate,
        int frozenFare,
        int freezeDeposit,
        int validityHours,
        String expiresAt,
        String status,
        String travelerEmail
) {
    @JsonProperty("freezeReference")
    public String freezeReference() {
        return freezeToken;
    }

    @JsonProperty("expirationTime")
    public String expirationTime() {
        return expiresAt;
    }
}
