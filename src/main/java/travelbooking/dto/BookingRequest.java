package travelbooking.dto;

/**
 * Request payload for confirming a simulated flight booking.
 */
public record BookingRequest(
        String flightId,
        String airline,
        String flightNumber,
        String origin,
        String destination,
        String departureDate,
        String departureTime,
        String arrivalTime,
        Integer passengers,
        String cabinClass,
        Integer totalFare,
        String passengerName,
        String passengerEmail,
        String passengerPhone
) {
    public int safePassengers() {
        return (passengers != null && passengers > 0) ? passengers : 1;
    }

    public int safeTotalFare() {
        return (totalFare != null && totalFare > 0) ? totalFare : 0;
    }
}
