package travelbooking.dto;

import travelbooking.model.FlightOffer;

import java.time.LocalDate;
import java.util.List;

/**
 * Structured response containing flight search results and metadata.
 */
public record FlightSearchResponse(
        boolean success,
        String origin,
        String destination,
        LocalDate departureDate,
        LocalDate returnDate,
        int passengers,
        String cabinClass,
        String tripType,
        int totalResults,
        List<FlightOffer> flights,
        List<String> availableAirlines,
        int minPrice,
        int maxPrice,
        String disclaimer
) {}
