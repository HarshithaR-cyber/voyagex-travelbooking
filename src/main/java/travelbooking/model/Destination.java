package travelbooking.model;

/**
 * Represents a popular travel destination.
 */
public record Destination(
        String id,
        String city,
        String country,
        String tag,
        int startingPrice,
        String currency,
        String imageUrl,
        double rating,
        int reviewCount,
        String flightDuration,
        String badge,
        String category
) {}
