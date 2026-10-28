package travelbooking.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Customer review and multi-criteria rating model.
 */
public record TravelReview(
        String id,
        String targetType, // HOTEL, FLIGHT, PLATFORM
        String targetId,
        String targetName,
        String authorName,
        String authorLocation,
        int rating,
        double cleanlinessScore,
        double serviceScore,
        double valueScore,
        String title,
        String comment,
        String createdDate,
        boolean verifiedBooking,
        List<String> photoUrls,
        String hostReply,
        int flagCount,
        int helpfulVotes
) {
    @JsonProperty("reviewerName")
    public String reviewerName() { return authorName; }

    @JsonProperty("itemName")
    public String itemName() { return targetName; }

    @JsonProperty("comments")
    public String comments() { return comment; }

    @JsonProperty("photoUrl")
    public String photoUrl() { return (photoUrls != null && !photoUrls.isEmpty()) ? photoUrls.get(0) : null; }
}
