package travelbooking.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * AI/rule-based personalized recommendation with contextual explanation.
 */
public record PersonalizedRecommendation(
        String id,
        String type, // DESTINATION, HOTEL, FLIGHT_PACKAGE
        String title,
        String subtitle,
        String location,
        String imageUrl,
        int priceFrom,
        int matchScore,
        String recommendationReason,
        List<String> tags
) {
    @JsonProperty("destinationName")
    public String destinationName() { return title; }

    @JsonProperty("country")
    public String country() { return subtitle; }

    @JsonProperty("reason")
    public String reason() { return recommendationReason; }

    @JsonProperty("startingPrice")
    public int startingPrice() { return priceFrom; }
}
