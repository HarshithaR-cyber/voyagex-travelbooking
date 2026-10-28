package travelbooking.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Detailed representation of a simulated flight offer.
 */
public record FlightOffer(
        String id,
        String airline,
        String airlineCode,
        String flightNumber,
        String originCode,
        String originCity,
        String destinationCode,
        String destinationCity,
        String departureTime,
        String arrivalTime,
        String duration,
        int durationMinutes,
        boolean isNonStop,
        String layoverInfo,
        int price,
        int totalFare,
        String currency,
        String cabinClass,
        int seatsAvailable,
        String aircraft,
        String badge,
        String baggageAllowance,
        String fareConditions,
        String recommendationCategory
) {
    public FlightOffer(String id, String airline, String airlineCode, String flightNumber,
                       String originCode, String originCity, String destinationCode, String destinationCity,
                       String departureTime, String arrivalTime, String duration, int durationMinutes,
                       boolean isNonStop, String layoverInfo, int price, int totalFare, String currency,
                       String cabinClass, int seatsAvailable, String aircraft, String badge) {
        this(id, airline, airlineCode, flightNumber, originCode, originCity, destinationCode, destinationCity,
                departureTime, arrivalTime, duration, durationMinutes, isNonStop, layoverInfo, price, totalFare,
                currency, cabinClass, seatsAvailable, aircraft, badge,
                "Cabin: 7kg • Checked: 23kg included",
                "Complimentary meals & beverages • Change fees apply",
                isNonStop ? "NONSTOP" : "CHEAPEST");
    }

    @JsonProperty("stopsCount")
    public int stopsCount() {
        return isNonStop ? 0 : 1;
    }
}
