package travelbooking.model;

/**
 * Live flight status tracking details including delay reasons and terminal info.
 */
public record FlightStatusInfo(
        String flightNumber,
        String airline,
        String origin,
        String destination,
        String scheduledDeparture,
        String actualDeparture,
        String scheduledArrival,
        String estimatedArrival,
        String status, // ON_TIME, DELAYED, BOARDING, DEPARTED, LANDED, CANCELLED
        int delayMinutes,
        String delayReason,
        String terminal,
        String gate,
        String baggageCarousel,
        String aircraft
) {}
