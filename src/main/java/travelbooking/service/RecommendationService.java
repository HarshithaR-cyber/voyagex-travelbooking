package travelbooking.service;

import org.springframework.stereotype.Service;
import travelbooking.dto.RecommendationQuery;
import travelbooking.model.PersonalizedRecommendation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RecommendationService {

    private final Map<String, Integer> feedbackHelpfulCount = new ConcurrentHashMap<>();

    public List<PersonalizedRecommendation> getRecommendations(RecommendationQuery query) {
        String style = query != null && query.travelStyle() != null ? query.travelStyle().toUpperCase() : "ROMANTIC";
        String vibe = query != null && query.preferredVibe() != null ? query.preferredVibe().toUpperCase() : "BEACH";

        List<PersonalizedRecommendation> recs = new ArrayList<>();

        if ("ROMANTIC".equals(style) || "BEACH".equals(vibe)) {
            recs.add(new PersonalizedRecommendation(
                    "rec-1", "DESTINATION", "Santorini & Cyclades Getaway", "Greece",
                    "Oia & Fira", "https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?auto=format&fit=crop&w=800&q=80",
                    429, 98,
                    "Selected because you indicated a preference for Romantic getaways with sunset viewpoints and Mediterranean beaches.",
                    List.of("Romantic Sunsets", "Caldera Suites", "Island Cruise")
            ));
            recs.add(new PersonalizedRecommendation(
                    "rec-2", "HOTEL", "Astra Caldera Luxury Suites", "Santorini, Greece",
                    "Imerovigli Cliffside", "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80",
                    320, 96,
                    "Pairs with your Santorini destination preference: rated 9.8 by couples for anniversary and honeymoon privacy.",
                    List.of("Infinity Pool", "Private Terrace", "Breakfast Included")
            ));
        }

        if ("ADVENTURE".equals(style) || "MOUNTAIN".equals(vibe)) {
            recs.add(new PersonalizedRecommendation(
                    "rec-3", "DESTINATION", "Interlaken Alpine Expedition", "Switzerland",
                    "Jungfrau Region", "https://images.unsplash.com/photo-1530122037265-a5f1f91d3b99?auto=format&fit=crop&w=800&q=80",
                    590, 97,
                    "Chosen for your Adventure style: premier European hub for alpine paragliding, glacier hikes, and scenic mountain railways.",
                    List.of("Alpine Hikes", "Glacier Express", "Eco-Resorts")
            ));
        }

        if ("LUXURY".equals(style) || "CITY".equals(vibe)) {
            recs.add(new PersonalizedRecommendation(
                    "rec-4", "DESTINATION", "Dubai Futuristic Skyline & Oasis", "United Arab Emirates",
                    "Downtown & Palm Jumeirah", "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=800&q=80",
                    560, 95,
                    "Matches your Luxury city profile: 5-star private beach retreats, helipad transfers, and world-class culinary dining.",
                    List.of("Luxury Shopping", "Desert Safari", "Michelin Dining")
            ));
            recs.add(new PersonalizedRecommendation(
                    "rec-5", "HOTEL", "Grand Hyatt Shinjuku Tower", "Tokyo, Japan",
                    "Shinjuku City Center", "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80",
                    280, 93,
                    "Top-rated luxury stay in Tokyo offering direct subway access and high-floor panoramic skyline sightlines.",
                    List.of("Skyline Views", "Rooftop Lounge", "Subway Connected")
            ));
        }

        if (recs.isEmpty()) {
            recs.add(new PersonalizedRecommendation(
                    "rec-6", "DESTINATION", "Parisian Art & Historic Cafes", "France",
                    "Central Paris", "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=800&q=80",
                    389, 92,
                    "Evergreen traveler favorite combining world-renowned galleries, charming bistros, and iconic architecture.",
                    List.of("Art & Culture", "Gastronomy", "Historic Landmarks")
            ));
        }

        return recs;
    }

    public boolean recordFeedback(String recId, boolean isHelpful) {
        if (isHelpful) {
            feedbackHelpfulCount.merge(recId, 1, Integer::sum);
        }
        return true;
    }
}
