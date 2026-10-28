package travelbooking.model;

import java.util.List;

/**
 * Cabin seat map configuration for a specific flight.
 */
public record SeatMap(
        String flightNumber,
        String airline,
        String aircraft,
        int totalRows,
        List<String> columnLabels,
        List<SeatInfo> seats
) {}
