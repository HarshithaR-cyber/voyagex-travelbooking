package travelbooking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import travelbooking.dto.ReviewSubmissionRequest;
import travelbooking.model.TravelReview;
import travelbooking.service.ReviewService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewApiController {

    private final ReviewService reviewService;

    public ReviewApiController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<List<TravelReview>> getReviews(
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String itemType,
            @RequestParam(required = false) String rating,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false, defaultValue = "recent") String sortBy
    ) {
        String effectiveType = (targetType != null && !targetType.isBlank()) ? targetType : (itemType != null ? itemType : "ALL");
        Integer effectiveMinRating = minRating;
        if (effectiveMinRating == null && rating != null && !rating.isBlank() && !"ALL".equalsIgnoreCase(rating)) {
            try {
                effectiveMinRating = Integer.parseInt(rating);
            } catch (NumberFormatException ignored) {}
        }
        return ResponseEntity.ok(reviewService.getReviews(effectiveType, effectiveMinRating, sortBy));
    }

    @PostMapping
    public ResponseEntity<TravelReview> submitReview(@RequestBody ReviewSubmissionRequest req) {
        return ResponseEntity.ok(reviewService.submitReview(req));
    }

    @PostMapping("/{id}/report")
    public ResponseEntity<Map<String, Object>> reportReview(@PathVariable String id) {
        reviewService.flagReview(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Thank you. This review has been flagged for moderator review."
        ));
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<Map<String, Object>> addReply(
            @PathVariable String id,
            @RequestBody Map<String, String> body
    ) {
        String replyText = body.get("reply");
        boolean success = reviewService.addReply(id, replyText);
        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "Host reply posted." : "Review not found."
        ));
    }
}
