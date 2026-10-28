package travelbooking.service;

import org.springframework.stereotype.Service;
import travelbooking.dto.ReviewSubmissionRequest;
import travelbooking.model.TravelReview;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final List<TravelReview> reviews = new CopyOnWriteArrayList<>();
    private final Map<String, Integer> reviewFlags = new ConcurrentHashMap<>();

    public ReviewService() {
        initReviews();
    }

    private void initReviews() {
        reviews.add(new TravelReview(
                "rev-1", "HOTEL", "hotel-1", "Astra Caldera Luxury Suites",
                "Elena Rostova", "Frankfurt, Germany", 5, 5.0, 4.9, 4.8,
                "Breathtaking Caldera Sunset & Impeccable Hospitality",
                "Our stay exceeded every expectation. The private plunge pool looking over the sunset in Oia is unforgettable. The staff brought fresh Greek breakfast every morning to our terrace. Will return next anniversary!",
                "2026-10-18", true,
                List.of("https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?auto=format&fit=crop&w=400&q=80"),
                "Thank you Elena! It was an absolute pleasure hosting you and celebrating your special milestone. Safe travels! – Astra Management",
                0, 42
        ));

        reviews.add(new TravelReview(
                "rev-2", "FLIGHT", "fl-101", "Air France (AF 023)",
                "Marcus Chen", "San Francisco, USA", 5, 4.8, 5.0, 4.9,
                "Smooth Transatlantic Crossing in Business Class",
                "Boarding was on time, cabin attendants were gracious and attentive. The lie-flat seat on the 777 gave me 6 uninterrupted hours of sleep before landing at CDG.",
                "2026-10-22", true,
                List.of(),
                "Thank you Marcus for choosing Air France. We look forward to welcoming you back aboard soon! – Air France Customer Care",
                0, 19
        ));

        reviews.add(new TravelReview(
                "rev-3", "HOTEL", "hotel-2", "Grand Hyatt Shinjuku Tower",
                "Chloe Davenport", "Sydney, Australia", 5, 4.9, 4.8, 4.7,
                "Direct Subway Access & Stunning Skyline Sightlines",
                "Location is unbeatable right in Shinjuku. The high floor premier room overlooked the glowing neon skyline. The concierge arranged our teamLab tickets within an hour.",
                "2026-10-14", true,
                List.of("https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=400&q=80"),
                null, 0, 15
        ));

        reviews.add(new TravelReview(
                "rev-4", "FLIGHT", "fl-102", "Delta Air Lines (DL 406)",
                "Siddharth Rao", "Bengaluru, India", 4, 4.5, 4.2, 4.0,
                "Reliable Flight with Excellent In-flight Wi-Fi",
                "Clean cabin and fast streaming Wi-Fi. The meal was tasty though dinner service was a bit delayed due to turbulence over Newfoundland.",
                "2026-10-09", true,
                List.of(),
                "Thanks for flying Delta Siddharth! Glad the Wi-Fi and seat comfort met your needs. – Delta Support",
                0, 8
        ));
    }

    public List<TravelReview> getReviews(String targetType, Integer minRating, String sortBy) {
        return reviews.stream()
                .filter(r -> {
                    if (targetType != null && !targetType.isBlank() && !"ALL".equalsIgnoreCase(targetType)) {
                        return r.targetType().equalsIgnoreCase(targetType);
                    }
                    return true;
                })
                .filter(r -> {
                    if (minRating != null && minRating > 0) {
                        return r.rating() >= minRating;
                    }
                    return true;
                })
                .sorted((a, b) -> {
                    String sort = sortBy != null ? sortBy.toLowerCase() : "recent";
                    return switch (sort) {
                        case "highest" -> Integer.compare(b.rating(), a.rating());
                        case "lowest" -> Integer.compare(a.rating(), b.rating());
                        default -> b.createdDate().compareTo(a.createdDate());
                    };
                })
                .collect(Collectors.toList());
    }

    public TravelReview submitReview(ReviewSubmissionRequest req) {
        String id = "rev-" + (reviews.size() + 1) + "-" + System.currentTimeMillis() % 1000;
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        // Photo URL safety check
        List<String> validatedPhotos = new ArrayList<>();
        if (req.photoUrls() != null) {
            for (String url : req.photoUrls()) {
                if (url != null && !url.isBlank() && (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("data:image/"))) {
                    validatedPhotos.add(url.trim());
                }
            }
        }

        TravelReview review = new TravelReview(
                id,
                req.targetType() != null ? req.targetType().toUpperCase() : "PLATFORM",
                req.targetId() != null ? req.targetId() : "gen-1",
                req.targetName() != null ? req.targetName() : "VoyageX Booking",
                (req.authorName() != null && !req.authorName().isBlank()) ? req.authorName() : "Verified Guest",
                (req.authorLocation() != null && !req.authorLocation().isBlank()) ? req.authorLocation() : "Global Traveler",
                req.safeRating(),
                req.safeCleanliness(),
                req.safeService(),
                req.safeValue(),
                (req.title() != null && !req.title().isBlank()) ? req.title() : "Outstanding Experience",
                (req.comment() != null && !req.comment().isBlank()) ? req.comment() : "Everything was seamless from booking to return flight.",
                today,
                true,
                validatedPhotos,
                null,
                0,
                0
        );

        reviews.add(0, review);
        return review;
    }

    public boolean flagReview(String reviewId) {
        reviewFlags.merge(reviewId, 1, Integer::sum);
        return true;
    }

    public boolean addReply(String reviewId, String replyText) {
        for (int i = 0; i < reviews.size(); i++) {
            TravelReview r = reviews.get(i);
            if (r.id().equalsIgnoreCase(reviewId)) {
                reviews.set(i, new TravelReview(
                        r.id(), r.targetType(), r.targetId(), r.targetName(),
                        r.authorName(), r.authorLocation(), r.rating(),
                        r.cleanlinessScore(), r.serviceScore(), r.valueScore(),
                        r.title(), r.comment(), r.createdDate(), r.verifiedBooking(),
                        r.photoUrls(), replyText, r.flagCount(), r.helpfulVotes()
                ));
                return true;
            }
        }
        return false;
    }
}
