package travelbooking.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents a hotel, boutique stay, or luxury resort.
 */
public record Hotel(
        String id,
        String name,
        String city,
        String country,
        int stars,
        double rating,
        String ratingLabel,
        int reviewCount,
        int pricePerNight,
        String currency,
        String imageUrl,
        List<String> amenities,
        String badge,
        String category,             // TOP_RATED, BEST_VALUE, LUXURY, FAMILY_FRIENDLY, BUSINESS, RESORTS, RECOMMENDED
        String availabilityStatus,   // Available Today, Only 2 Rooms Left, Instant Confirmation
        String description
) {
    public Hotel(String id, String name, String city, String country, int stars, double rating,
                 String ratingLabel, int reviewCount, int pricePerNight, String currency,
                 String imageUrl, List<String> amenities, String badge) {
        this(id, name, city, country, stars, rating, ratingLabel, reviewCount, pricePerNight, currency,
                imageUrl, amenities, badge, "LUXURY", "Available Today",
                "Experience world-class hospitality, curated room amenities, and personalized concierge care.");
    }

    @JsonProperty("location")
    public String location() {
        return (city != null && !city.isBlank()) ? city + ", " + country : (country != null ? country : "Global Destination");
    }
}
