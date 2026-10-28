package travelbooking.service;

import org.springframework.stereotype.Service;
import travelbooking.model.VipDeal;

import java.util.*;

@Service
public class VipDealsService {

    private final List<VipDeal> deals = new ArrayList<>();

    public VipDealsService() {
        initDeals();
    }

    private void initDeals() {
        deals.add(new VipDeal(
                "deal-1",
                "Santorini Cliffside Honeymoon Escape",
                "LUXURY_ESCAPES",
                "Luxury Escape",
                "Santorini",
                "Greece",
                "https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?auto=format&fit=crop&w=800&q=80",
                890,
                620,
                List.of("Private cave plunge pool", "Daily champagne breakfast", "Chauffeured airport transfer", "Sunset catamaran cruise"),
                "Eligible for VoyageX members booking 3+ consecutive nights",
                "Travel valid: Nov 1, 2026 – Dec 20, 2026",
                "BUNDLE",
                "VOYAGE30",
                true,
                "Indulge in Cycladic architecture overlooking the azure Aegean sea. Includes private plunge suite at Astra Caldera, Greek wine tasting, and premium airport concierge."
        ));

        deals.add(new VipDeal(
                "deal-2",
                "Tokyo Skyline & High-Speed Rail Explorer",
                "FLIGHT_HOTEL_BUNDLES",
                "Flight + Hotel Bundle",
                "Tokyo",
                "Japan",
                "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=800&q=80",
                1420,
                995,
                List.of("Flight + 4-star Shinjuku tower stay", "Complimentary pocket WiFi", "Direct Narita Skyliner pass", "teamLab borderless exhibition ticket"),
                "Applies automatically when reserving flight and stay together",
                "Travel valid: Nov 10, 2026 – Feb 28, 2027",
                "BUNDLE",
                "VOYAGE30",
                true,
                "The ultimate urban adventure pairing non-stop transatlantic flight with luxury high-floor accommodations in vibrant Shinjuku, plus full connectivity across Tokyo."
        ));

        deals.add(new VipDeal(
                "deal-3",
                "Parisian Autumn Boutique Weekend",
                "WEEKEND_GETAWAYS",
                "Weekend Getaway",
                "Paris",
                "France",
                "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=800&q=80",
                650,
                485,
                List.of("Champs-Élysées boutique suite", "Priority Louvre & Eiffel access", "Morning café & croissant basket", "Late 2:00 PM Sunday checkout"),
                "Valid for Friday to Sunday weekend departures",
                "Travel valid: Fridays through Sundays in Nov 2026",
                "HOTEL",
                "WELCOME15",
                true,
                "Spend a romantic autumn weekend walking the Seine, tasting warm pastries in Montmartre, and enjoying late hotel checkout."
        ));

        deals.add(new VipDeal(
                "deal-4",
                "Dubai Executive Lounge & Business Class Package",
                "BUSINESS_OFFERS",
                "Business Traveller Offer",
                "Dubai",
                "United Arab Emirates",
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=800&q=80",
                1180,
                885,
                List.of("VIP airport lounge access", "Downtown chauffeured Tesla transfer", "24/7 business center & boardroom", "Complimentary suit pressing"),
                "Corporate and solo business travelers with valid corporate ID or VoyageX Plus tier",
                "Travel valid: All weekdays through Dec 31, 2026",
                "FLIGHT",
                "VOYAGE30",
                true,
                "Maximize executive productivity with premium lounge access, in-flight high-speed satellite Wi-Fi, and downtown connectivity."
        ));

        deals.add(new VipDeal(
                "deal-5",
                "Swiss Alpine Spring Glacier & Ski Pass",
                "EARLY_BOOKING",
                "Early Booking Offer",
                "Interlaken",
                "Switzerland",
                "https://images.unsplash.com/photo-1530122037265-a5f1f91d3b99?auto=format&fit=crop&w=800&q=80",
                1350,
                945,
                List.of("Jungfrau railway summit pass", "Heated ski gear locker", "Thermal wellness bath access", "Swiss fondue dinner experience"),
                "Book 45+ days in advance of departure",
                "Travel valid: Jan 15, 2027 – April 30, 2027",
                "BUNDLE",
                "VOYAGE30",
                true,
                "Lock in early-bird savings for the 2027 Swiss ski season with panoramic train passes, mountain resort lodging, and après-ski thermal spa access."
        ));

        deals.add(new VipDeal(
                "deal-6",
                "Bali Rainforest Villa (Last-Minute Departure)",
                "LAST_MINUTE",
                "Last-Minute Deal",
                "Bali",
                "Indonesia",
                "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=800&q=80",
                590,
                380,
                List.of("Complimentary jungle villa upgrade", "Daily Balinese herbal massage", "Airport pickup & dropoff", "Ubud temple excursion"),
                "Immediate booking for departures within the next 10 days",
                "Travel valid: Departures within 10 days of booking",
                "HOTEL",
                "VOYAGE30",
                true,
                "Unbeatable spontaneous getaway savings. Escape to a private villa overlooking Ubud's sacred rice terraces with daily spa treatments."
        ));
    }

    public List<VipDeal> getDeals(String category) {
        if (category == null || category.isBlank() || "ALL".equalsIgnoreCase(category)) {
            return Collections.unmodifiableList(deals);
        }
        return deals.stream()
                .filter(d -> d.category().equalsIgnoreCase(category))
                .toList();
    }

    public Optional<VipDeal> getDealById(String id) {
        if (id == null) return Optional.empty();
        return deals.stream().filter(d -> d.id().equalsIgnoreCase(id)).findFirst();
    }

    public Map<String, Object> validateAndQuoteDeal(String dealId, int travelerCount) {
        Optional<VipDeal> dealOpt = getDealById(dealId);
        if (dealOpt.isEmpty()) {
            return Map.of("valid", false, "message", "Deal not found");
        }

        VipDeal deal = dealOpt.get();
        if (!deal.bookable()) {
            return Map.of("valid", false, "message", "This promotional deal is no longer bookable.");
        }

        int count = Math.max(1, travelerCount);
        int totalOriginal = deal.originalPrice() * count;
        int totalDiscounted = deal.discountedPrice() * count;
        int savings = totalOriginal - totalDiscounted;

        Map<String, Object> quote = new LinkedHashMap<>();
        quote.put("valid", true);
        quote.put("dealId", deal.id());
        quote.put("title", deal.title());
        quote.put("destination", deal.destination());
        quote.put("travelers", count);
        quote.put("unitPrice", deal.discountedPrice());
        quote.put("totalOriginal", totalOriginal);
        quote.put("totalDiscounted", totalDiscounted);
        quote.put("totalSavings", savings);
        quote.put("promoCode", deal.promoCode());
        quote.put("serviceType", deal.serviceType());

        return quote;
    }
}
