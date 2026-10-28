package travelbooking.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Model representing a VoyageX VIP travel deal or curated bundle.
 */
public record VipDeal(
        String id,
        String title,
        String category,          // LUXURY_ESCAPES, FLIGHT_HOTEL_BUNDLES, WEEKEND_GETAWAYS, BUSINESS_OFFERS, EARLY_BOOKING, LAST_MINUTE
        String categoryLabel,
        String destination,
        String country,
        String imageUrl,
        int originalPrice,
        int discountedPrice,
        List<String> benefits,
        String eligibility,
        String validityPeriod,
        String serviceType,       // BUNDLE, FLIGHT, HOTEL
        String promoCode,
        boolean bookable,
        String description
) {
    @JsonProperty("discountPercentage")
    public int discountPercentage() {
        if (originalPrice <= 0 || discountedPrice >= originalPrice) {
            return 0;
        }
        return (int) Math.round(((double) (originalPrice - discountedPrice) / originalPrice) * 100);
    }
}
