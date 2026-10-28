package travelbooking.dto;

/**
 * Response for a simulated flight booking confirmation.
 */
public record BookingResponse(
        boolean success,
        String bookingReference,
        String status,
        String passengerName,
        String passengerEmail,
        String flightNumber,
        String airline,
        String route,
        String departureDate,
        String departureTime,
        int passengers,
        String cabinClass,
        int totalFare,
        String currency,
        String message,
        String disclaimer
) {}
