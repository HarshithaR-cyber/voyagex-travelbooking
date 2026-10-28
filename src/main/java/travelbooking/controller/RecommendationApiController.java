package travelbooking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import travelbooking.dto.RecommendationQuery;
import travelbooking.model.PersonalizedRecommendation;
import travelbooking.service.RecommendationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*")
public class RecommendationApiController {

    private final RecommendationService recommendationService;

    public RecommendationApiController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping({"", "/", "/match"})
    public ResponseEntity<List<PersonalizedRecommendation>> getRecommendations(@RequestBody(required = false) RecommendationQuery query) {
        return ResponseEntity.ok(recommendationService.getRecommendations(query != null ? query : new RecommendationQuery("ROMANTIC", "BEACH", "LUXURY", "COUPLE")));
    }

    @GetMapping({"", "/", "/match"})
    public ResponseEntity<List<PersonalizedRecommendation>> getRecommendationsGet(
            @RequestParam(required = false, defaultValue = "ROMANTIC") String travelStyle,
            @RequestParam(required = false, defaultValue = "BEACH") String settingVibe,
            @RequestParam(required = false, defaultValue = "LUXURY") String budgetPreference,
            @RequestParam(required = false, defaultValue = "COUPLE") String companionType) {
        return ResponseEntity.ok(recommendationService.getRecommendations(new RecommendationQuery(travelStyle, settingVibe, budgetPreference, companionType)));
    }

    @PostMapping("/feedback")
    public ResponseEntity<Map<String, Object>> submitFeedback(@RequestBody Map<String, Object> body) {
        String recId = (String) body.get("recId");
        boolean helpful = Boolean.TRUE.equals(body.get("helpful"));
        recommendationService.recordFeedback(recId, helpful);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Thank you for helping us improve our recommendations!"
        ));
    }
}
