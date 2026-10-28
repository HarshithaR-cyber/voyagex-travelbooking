package travelbooking.service;

import org.springframework.stereotype.Service;
import travelbooking.dto.HotelBookingRequest;
import travelbooking.dto.SeatSelectionRequest;
import travelbooking.model.HotelBookingRecord;
import travelbooking.model.HotelRoom;
import travelbooking.model.SeatInfo;
import travelbooking.model.SeatMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SeatAndRoomService {

    private final Map<String, List<HotelRoom>> hotelRooms = new ConcurrentHashMap<>();
    private final Map<String, HotelBookingRecord> hotelBookings = new ConcurrentHashMap<>();
    private final Map<String, SeatSelectionRequest> seatAssignments = new ConcurrentHashMap<>();

    public SeatAndRoomService() {
        initHotelRooms();
    }

    private void initHotelRooms() {
        // Rooms for Astra Caldera Suites (hotel-1)
        hotelRooms.put("hotel-1", List.of(
                new HotelRoom("rm-101", "hotel-1", "Deluxe Caldera View Suite", "1 King Bed", 42, 320, 4,
                        List.of("Panoramic Sunset Balcony", "Marble Jacuzzi", "Espresso Machine", "Complimentary Breakfast"),
                        "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80"),
                new HotelRoom("rm-102", "hotel-1", "Executive Infinity Plunge Suite", "1 King Bed + Daybed", 60, 480, 2,
                        List.of("Private Heated Plunge Pool", "Caldera Sunset View", "Butler Service", "VIP Airport Transfer"),
                        "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=800&q=80"),
                new HotelRoom("rm-103", "hotel-1", "Honeymoon Cliffside Villa", "Super King Bed", 85, 750, 1,
                        List.of("Private Infinity Pool", "Cave Spa Tub", "Champagne Upon Arrival", "Private Terrace Dining"),
                        "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80")
        ));

        // Rooms for Grand Hyatt Shinjuku (hotel-2)
        hotelRooms.put("hotel-2", List.of(
                new HotelRoom("rm-201", "hotel-2", "Club Tower King Room", "1 King Bed", 38, 280, 6,
                        List.of("Tokyo Skyline View", "Deep Soaking Tub", "High-speed WiFi", "Lounge Access"),
                        "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80"),
                new HotelRoom("rm-202", "hotel-2", "Premier Shinjuku Skyline Suite", "1 King Bed + Living Area", 65, 430, 3,
                        List.of("Fuji View on Clear Days", "Executive Club Perks", "Bose Sound System", "Nespresso"),
                        "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80")
        ));

        // Rooms for Hôtel Plaza Elysée (hotel-3)
        hotelRooms.put("hotel-3", List.of(
                new HotelRoom("rm-301", "hotel-3", "Prestige Parisian Balcony Room", "1 Queen Bed", 32, 390, 5,
                        List.of("Courtyard / Street View", "Diptyque Toiletries", "Rain Shower", "Parisian Breakfast"),
                        "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=800&q=80"),
                new HotelRoom("rm-302", "hotel-3", "Eiffel Signature Panoramic Suite", "1 King Bed", 58, 620, 2,
                        List.of("Direct Eiffel Tower Sightline", "Haute Couture Decor", "Marble Bath", "24/7 Concierge"),
                        "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=800&q=80")
        ));

        // Rooms for The Palm Royal Oasis (hotel-4)
        hotelRooms.put("hotel-4", List.of(
                new HotelRoom("rm-401", "hotel-4", "Ocean Terrace Palm Room", "1 King or 2 Queen", 48, 410, 8,
                        List.of("Arabian Gulf Vista", "Private Balcony", "Walk-in Dressing Room", "Resort Waterpark Access"),
                        "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=800&q=80"),
                new HotelRoom("rm-402", "hotel-4", "Royal Beachfront Pavilion", "1 Super King Bed", 95, 820, 2,
                        List.of("Direct Private Beach Steps", "Private Cabana", "Personal Butler", "Complimentary Spa Ritual"),
                        "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80")
        ));

        // Rooms for Como Shambhala Tropical Estate (hotel-5)
        hotelRooms.put("hotel-5", List.of(
                new HotelRoom("rm-501", "hotel-5", "Ayung River Valley Teak Room", "1 King Bed", 45, 310, 4,
                        List.of("Rainforest Balcony", "Outdoor Stone Shower", "Morning Yoga", "Organic Breakfast"),
                        "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=800&q=80"),
                new HotelRoom("rm-502", "hotel-5", "Private Waterfall Pool Residence", "1 King Bed", 80, 560, 2,
                        List.of("Private Plunge Pool", "Hydrotherapy Access", "Personal Wellness Guide", "Airport Transfer"),
                        "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=800&q=80")
        ));

        // Rooms for Kyoto Machiya Heritage (hotel-6)
        hotelRooms.put("hotel-6", List.of(
                new HotelRoom("rm-601", "hotel-6", "Traditional Tatami Garden Room", "2 Futon Mattresses", 28, 155, 5,
                        List.of("Zen Rock Garden View", "Shoji Paper Screens", "Green Tea Set", "Complimentary Bike"),
                        "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?auto=format&fit=crop&w=800&q=80"),
                new HotelRoom("rm-602", "hotel-6", "Hinoki Cedar Bath Suite", "1 Queen Bed + 1 Futon", 42, 235, 2,
                        List.of("Aromatic Cedar Onsen Tub", "Inner Courtyard Access", "Yukata Robes", "Gion Walking Tour"),
                        "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=800&q=80")
        ));

        // Rooms for Atlantis Palm Aquaventure Villa (hotel-7)
        hotelRooms.put("hotel-7", List.of(
                new HotelRoom("rm-701", "hotel-7", "Two-Bedroom Family Ocean Suite", "1 King + 2 Twin Beds", 75, 365, 6,
                        List.of("Unlimited Waterpark Access", "Kids Club Passes", "Ocean View Balcony", "Daily Breakfast Buffet"),
                        "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=800&q=80"),
                new HotelRoom("rm-702", "hotel-7", "Underwater Lagoon Master Suite", "1 King Bed", 90, 920, 1,
                        List.of("Floor-to-Ceiling Aquarium Windows", "Private Butler", "In-Suite Caviar Breakfast"),
                        "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=800&q=80")
        ));

        // Rooms for Victoria-Jungfrau Alpine Grand Hotel (hotel-8)
        hotelRooms.put("hotel-8", List.of(
                new HotelRoom("rm-801", "hotel-8", "Superior Jungfrau View Alpine Room", "1 King Bed", 36, 440, 4,
                        List.of("Glacier Mountain View", "Nescens Spa Access", "Swiss Chocolate Welcome", "Ski Storage"),
                        "https://images.unsplash.com/photo-1530122037265-a5f1f91d3b99?auto=format&fit=crop&w=800&q=80"),
                new HotelRoom("rm-802", "hotel-8", "Tower Presidential Alpine Suite", "1 King Bed + Fireplace", 85, 880, 1,
                        List.of("360° Alpine Panorama", "Open Fireplace", "Private Sauna", "Helicopter Transfer Option"),
                        "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80")
        ));
    }

    /**
     * Generates a 6-row x 6-seat interactive cabin map for the chosen flight.
     */
    public SeatMap getSeatMap(String flightNumber, String aircraft) {
        List<String> cols = List.of("A", "B", "C", "D", "E", "F");
        List<SeatInfo> seats = new ArrayList<>();

        for (int row = 1; row <= 6; row++) {
            String tier;
            int price;
            if (row <= 2) {
                tier = "Preferred Front";
                price = 25;
            } else if (row == 3) {
                tier = "Extra Legroom Exit";
                price = 45;
            } else {
                tier = "Standard Economy";
                price = 0;
            }

            for (String col : cols) {
                String seatNum = row + col;
                boolean occupied = (row == 1 && col.equals("A")) ||
                                   (row == 2 && col.equals("D")) ||
                                   (row == 3 && col.equals("C")) ||
                                   (row == 5 && col.equals("B")) ||
                                   (row == 6 && col.equals("F"));

                List<String> features = new ArrayList<>();
                if (col.equals("A") || col.equals("F")) features.add("Window");
                if (col.equals("C") || col.equals("D")) features.add("Aisle");
                if (row == 3) features.add("Exit Row (Extra 6 inches)");
                features.add("USB Power");

                seats.add(new SeatInfo(seatNum, row, col, tier, price, !occupied, features));
            }
        }

        return new SeatMap(
                flightNumber != null ? flightNumber : "FL-101",
                "VoyageX Partner Airlines",
                aircraft != null ? aircraft : "Airbus A350-900",
                6, cols, seats
        );
    }

    public boolean reserveSeat(SeatSelectionRequest req) {
        String key = (req.bookingReference() != null ? req.bookingReference() : "DEMO") + "-" + req.seatNumber();
        seatAssignments.put(key, req);
        return true;
    }

    public List<HotelRoom> getRoomsForHotel(String hotelId) {
        return hotelRooms.getOrDefault(hotelId, hotelRooms.get("hotel-1"));
    }

    public HotelBookingRecord bookHotelRoom(HotelBookingRequest req) {
        String ref = "HT-" + ThreadLocalRandom.current().nextInt(10000, 99999);
        HotelBookingRecord record = new HotelBookingRecord(
                ref,
                req.hotelId() != null ? req.hotelId() : "hotel-1",
                req.hotelName() != null ? req.hotelName() : "Luxury Resort Stay",
                req.roomId() != null ? req.roomId() : "rm-101",
                req.roomName() != null ? req.roomName() : "Deluxe Room",
                req.checkInDate() != null ? req.checkInDate() : "2026-11-05",
                req.checkOutDate() != null ? req.checkOutDate() : "2026-11-08",
                req.safeNights(),
                req.safeGuests(),
                req.safeTotalAmount(),
                req.guestName() != null ? req.guestName() : "Valued Guest",
                req.guestEmail() != null ? req.guestEmail() : "guest@example.com",
                "CONFIRMED"
        );

        hotelBookings.put(ref, record);
        return record;
    }

    public Optional<HotelBookingRecord> getHotelBooking(String ref) {
        return Optional.ofNullable(hotelBookings.get(ref.trim().toUpperCase()));
    }
}
