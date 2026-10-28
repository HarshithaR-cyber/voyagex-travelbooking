package travelbooking.service;

import org.springframework.stereotype.Service;
import travelbooking.model.Destination;
import travelbooking.model.FlightOffer;
import travelbooking.model.Hotel;

import java.util.List;

@Service
public class TravelDataService {

    public List<Destination> getPopularDestinations() {
        return List.of(
                new Destination(
                        "dest-1", "Santorini", "Greece", "Cyclades Island", 429, "$",
                        "https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?auto=format&fit=crop&w=800&q=80",
                        4.96, 1840, "6h 20m", "Popular Choice", "Island"
                ),
                new Destination(
                        "dest-2", "Tokyo", "Japan", "Shinjuku & Shibuya", 649, "$",
                        "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=800&q=80",
                        4.94, 2950, "13h 45m", "Trending Now", "City"
                ),
                new Destination(
                        "dest-3", "Paris", "France", "City of Lights", 389, "$",
                        "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=800&q=80",
                        4.91, 3420, "7h 50m", "Top Romantic", "Romance"
                ),
                new Destination(
                        "dest-4", "Bali", "Indonesia", "Tropical Haven", 520, "$",
                        "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=800&q=80",
                        4.98, 2190, "18h 10m", "20% Off Flights", "Beach"
                ),
                new Destination(
                        "dest-5", "Rome", "Italy", "The Eternal City", 399, "$",
                        "https://images.unsplash.com/photo-1552832230-c0197dd311b5?auto=format&fit=crop&w=800&q=80",
                        4.89, 1780, "8h 30m", "Historic Wonder", "City"
                ),
                new Destination(
                        "dest-6", "Dubai", "United Arab Emirates", "Futuristic Skyline", 560, "$",
                        "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=800&q=80",
                        4.92, 2400, "12h 15m", "Luxury Hub", "City"
                ),
                new Destination(
                        "dest-7", "Maui, Hawaii", "United States", "Pacific Paradise", 480, "$",
                        "https://images.unsplash.com/photo-1542259009477-d625272157b7?auto=format&fit=crop&w=800&q=80",
                        4.95, 1320, "10h 05m", "Sunny Escape", "Beach"
                ),
                new Destination(
                        "dest-8", "Interlaken", "Switzerland", "Alpine Wonders", 590, "$",
                        "https://images.unsplash.com/photo-1530122037265-a5f1f91d3b99?auto=format&fit=crop&w=800&q=80",
                        4.97, 1640, "9h 10m", "Adventure Peak", "Adventure"
                )
        );
    }

    public List<Hotel> getFeaturedHotels() {
        return List.of(
                new Hotel(
                        "hotel-1",
                        "Astra Caldera Luxury Suites",
                        "Santorini",
                        "Greece",
                        5,
                        9.8,
                        "Exceptional",
                        840,
                        320,
                        "$",
                        "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80",
                        List.of("Infinity Pool", "Ocean Sunset View", "Caldera Spa", "Free Breakfast", "Airport Transfer"),
                        "Caldera View",
                        "TOP_RATED",
                        "Only 2 Suites Left",
                        "Perched on the rim of the Santorini caldera, Astra Suites offers tranquil cave-suites, private heated plunge pools, and uninterrupted views of world-renowned Oia sunsets."
                ),
                new Hotel(
                        "hotel-2",
                        "Grand Hyatt Shinjuku Tower",
                        "Tokyo",
                        "Japan",
                        5,
                        9.5,
                        "Superb",
                        1260,
                        280,
                        "$",
                        "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80",
                        List.of("Rooftop Lounge", "High-speed WiFi", "Michelin Dining", "Direct Subway Access", "Fitness Center"),
                        "City Landmark",
                        "BUSINESS",
                        "Available Today",
                        "Rising above the neon skyline of Shinjuku, this premier tower connects directly to Tokyo Metro and offers soundproof executive suites with panoramic views of Mt. Fuji."
                ),
                new Hotel(
                        "hotel-3",
                        "Hôtel Plaza Elysée & Spa",
                        "Paris",
                        "France",
                        5,
                        9.6,
                        "Wonderful",
                        920,
                        390,
                        "$",
                        "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=800&q=80",
                        List.of("Eiffel Tower Views", "Haute Cuisine", "Boutique Spa", "Concierge Service", "Marble Baths"),
                        "Boutique Luxury",
                        "RECOMMENDED",
                        "Instant Confirmation",
                        "Nestled steps from the Champs-Élysées, Hôtel Plaza blends Haussmannian glamour with bespoke Dior spa treatments and private terraces facing the Eiffel Tower."
                ),
                new Hotel(
                        "hotel-4",
                        "The Palm Royal Oasis Resort",
                        "Dubai",
                        "United Arab Emirates",
                        5,
                        9.7,
                        "Exceptional",
                        1680,
                        410,
                        "$",
                        "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=800&q=80",
                        List.of("Private Beach", "Aquatic Park", "Helipad Access", "9 Gourmet Restaurants", "Butler Service"),
                        "Signature Choice",
                        "LUXURY",
                        "Only 3 Villas Left",
                        "A sprawling architectural wonder on the Palm Jumeirah with 1.5 km of private white sand, underwater dining, and around-the-clock personal butler hospitality."
                ),
                new Hotel(
                        "hotel-5",
                        "Como Shambhala Tropical Estate",
                        "Bali",
                        "Indonesia",
                        5,
                        9.7,
                        "Exceptional",
                        760,
                        310,
                        "$",
                        "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=800&q=80",
                        List.of("Infinity Rainforest Pool", "Holistic Spa", "Organic Dining", "Yoga Pavilion", "River Treks"),
                        "Serene Sanctuary",
                        "RESORTS",
                        "Available Today",
                        "Surrounded by lush Ubud river valleys, Como Shambhala integrates holistic wellness programs, outdoor vitality hydrotherapy pools, and private teakwood pavilions."
                ),
                new Hotel(
                        "hotel-6",
                        "Kyoto Machiya Heritage Residence",
                        "Kyoto",
                        "Japan",
                        4,
                        9.4,
                        "Exceptional",
                        640,
                        155,
                        "$",
                        "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?auto=format&fit=crop&w=800&q=80",
                        List.of("Traditional Tatami", "Zen Garden", "Free Breakfast", "Bicycle Rentals", "High-speed WiFi"),
                        "Great Value",
                        "BEST_VALUE",
                        "Instant Confirmation",
                        "A restored 120-year-old wooden Machiya townhouse in historic Gion. Features authentic hinoki cedar soaking tubs, serene rock gardens, and unmatched affordability."
                ),
                new Hotel(
                        "hotel-7",
                        "Atlantis Palm Aquaventure Villa",
                        "Dubai",
                        "United Arab Emirates",
                        5,
                        9.6,
                        "Superb",
                        1420,
                        365,
                        "$",
                        "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=800&q=80",
                        List.of("Kids Club & Arcade", "Free Waterpark Access", "Family Suites", "Private Beach", "Buffet Breakfast"),
                        "Family Favorite",
                        "FAMILY_FRIENDLY",
                        "Available Today",
                        "The premier family retreat featuring unlimited complimentary access to the world's largest waterpark, interactive dolphin encounters, and spacious connected suites."
                ),
                new Hotel(
                        "hotel-8",
                        "Victoria-Jungfrau Alpine Grand Hotel",
                        "Interlaken",
                        "Switzerland",
                        5,
                        9.7,
                        "Exceptional",
                        980,
                        440,
                        "$",
                        "https://images.unsplash.com/photo-1530122037265-a5f1f91d3b99?auto=format&fit=crop&w=800&q=80",
                        List.of("Alpine Spa & Sauna", "Jungfrau View", "Fine Dining", "Ski Shuttles", "Heated Indoor Pool"),
                        "Alpine Wonder",
                        "RESORTS",
                        "Only 1 Room Left",
                        "A legendary Swiss palace situated between two crystal lakes at the foot of the snow-capped Jungfrau massif. Offers a 5,500 sqm Nescens wellness sanctuary."
                )
        );
    }

    public List<Hotel> filterHotels(String category, String location, Double minRating, Integer maxPrice, String amenity, String sortBy) {
        return getFeaturedHotels().stream()
                .filter(h -> {
                    if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
                        if ("RECOMMENDED".equalsIgnoreCase(category)) {
                            return "RECOMMENDED".equalsIgnoreCase(h.category()) || h.rating() >= 9.6;
                        }
                        return h.category().equalsIgnoreCase(category);
                    }
                    return true;
                })
                .filter(h -> {
                    if (location != null && !location.isBlank() && !"ALL".equalsIgnoreCase(location)) {
                        String locLower = location.toLowerCase();
                        return h.city().toLowerCase().contains(locLower) || h.country().toLowerCase().contains(locLower);
                    }
                    return true;
                })
                .filter(h -> {
                    if (minRating != null && minRating > 0) {
                        return h.rating() >= minRating;
                    }
                    return true;
                })
                .filter(h -> {
                    if (maxPrice != null && maxPrice > 0) {
                        return h.pricePerNight() <= maxPrice;
                    }
                    return true;
                })
                .filter(h -> {
                    if (amenity != null && !amenity.isBlank() && !"ALL".equalsIgnoreCase(amenity)) {
                        String aLower = amenity.toLowerCase();
                        return h.amenities().stream().anyMatch(a -> a.toLowerCase().contains(aLower));
                    }
                    return true;
                })
                .sorted((a, b) -> {
                    if ("price_asc".equalsIgnoreCase(sortBy)) {
                        return Integer.compare(a.pricePerNight(), b.pricePerNight());
                    } else if ("price_desc".equalsIgnoreCase(sortBy)) {
                        return Integer.compare(b.pricePerNight(), a.pricePerNight());
                    } else if ("rating_desc".equalsIgnoreCase(sortBy)) {
                        return Double.compare(b.rating(), a.rating());
                    }
                    return Double.compare(b.rating(), a.rating());
                })
                .toList();
    }

    public List<FlightOffer> getSampleFlightOffers(String origin, String destination, String cabinClass) {
        String dep = (origin != null && !origin.isBlank()) ? origin : "New York (JFK)";
        String arr = (destination != null && !destination.isBlank()) ? destination : "Paris (CDG)";
        String cabin = (cabinClass != null && !cabinClass.isBlank()) ? cabinClass : "Economy";

        return List.of(
                new FlightOffer(
                        "fl-101", "Air France", "AF", "AF 023", "JFK", dep, "CDG", arr,
                        "07:30 PM", "09:15 AM (+1)", "7h 45m", 465, true, "Non-stop",
                        485, 485, "$", cabin, 4, "Boeing 777-300ER", "Fastest Flight"
                ),
                new FlightOffer(
                        "fl-102", "Delta Air Lines", "DL", "DL 406", "JFK", dep, "CDG", arr,
                        "10:15 PM", "11:55 AM (+1)", "7h 40m", 460, true, "Non-stop",
                        460, 460, "$", cabin, 6, "Airbus A350-900", "Best Value"
                ),
                new FlightOffer(
                        "fl-103", "Emirates", "EK", "EK 202", "JFK", dep, "CDG", arr,
                        "04:20 PM", "07:35 AM (+1)", "9h 15m", 555, false, "1 stop in Dubai (DXB &bull; 2h layover)",
                        415, 415, "$", cabin, 3, "Airbus A380-800", "Cheapest Option"
                ),
                new FlightOffer(
                        "fl-104", "British Airways", "BA", "BA 178", "JFK", dep, "CDG", arr,
                        "08:45 PM", "10:30 AM (+1)", "7h 45m", 465, true, "Non-stop",
                        510, 510, "$", cabin, 5, "Boeing 787-9", "Top Rated Service"
                )
        );
    }
}
