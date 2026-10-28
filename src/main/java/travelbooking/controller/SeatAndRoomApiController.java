package travelbooking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import travelbooking.dto.HotelBookingRequest;
import travelbooking.dto.SeatSelectionRequest;
import travelbooking.model.Hotel;
import travelbooking.model.HotelBookingRecord;
import travelbooking.model.HotelRoom;
import travelbooking.model.SeatMap;
import travelbooking.service.SeatAndRoomService;
import travelbooking.service.TravelDataService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SeatAndRoomApiController {

    private final SeatAndRoomService seatAndRoomService;
    private final TravelDataService travelDataService;

    public SeatAndRoomApiController(SeatAndRoomService seatAndRoomService, TravelDataService travelDataService) {
        this.seatAndRoomService = seatAndRoomService;
        this.travelDataService = travelDataService;
    }

    @GetMapping("/hotels")
    public ResponseEntity<List<Hotel>> getHotels(
            @RequestParam(required = false, defaultValue = "ALL") String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String minRating,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) String amenity,
            @RequestParam(required = false, defaultValue = "recommended") String sortBy
    ) {
        Double parsedRating = null;
        if (minRating != null && !minRating.isBlank() && !minRating.equalsIgnoreCase("ALL")) {
            try {
                parsedRating = Double.parseDouble(minRating.trim());
            } catch (NumberFormatException ignored) {}
        }
        Integer parsedMaxPrice = null;
        if (maxPrice != null && !maxPrice.isBlank() && !maxPrice.equalsIgnoreCase("ALL")) {
            try {
                parsedMaxPrice = Integer.parseInt(maxPrice.trim());
            } catch (NumberFormatException ignored) {}
        }
        return ResponseEntity.ok(travelDataService.filterHotels(category, location, parsedRating, parsedMaxPrice, amenity, sortBy));
    }

    @GetMapping("/hotels/{hotelId}")
    public ResponseEntity<Hotel> getHotelById(@PathVariable String hotelId) {
        return travelDataService.getFeaturedHotels().stream()
                .filter(h -> h.id().equalsIgnoreCase(hotelId))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/hotels/{hotelId}/rooms")
    public ResponseEntity<List<HotelRoom>> getHotelRooms(@PathVariable String hotelId) {
        return ResponseEntity.ok(seatAndRoomService.getRoomsForHotel(hotelId));
    }

    @PostMapping("/hotels/book")
    public ResponseEntity<HotelBookingRecord> bookHotel(@RequestBody HotelBookingRequest req) {
        return ResponseEntity.ok(seatAndRoomService.bookHotelRoom(req));
    }

    @GetMapping("/hotels/bookings/{ref}")
    public ResponseEntity<?> getHotelBooking(@PathVariable String ref) {
        return seatAndRoomService.getHotelBooking(ref)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body((HotelBookingRecord) null));
    }

    @GetMapping("/seats/map")
    public ResponseEntity<SeatMap> getSeatMap(
            @RequestParam(required = false, defaultValue = "AI 101") String flightNumber,
            @RequestParam(required = false, defaultValue = "Airbus A350-900") String aircraft
    ) {
        return ResponseEntity.ok(seatAndRoomService.getSeatMap(flightNumber, aircraft));
    }

    @PostMapping("/seats/select")
    public ResponseEntity<Map<String, Object>> selectSeat(@RequestBody SeatSelectionRequest req) {
        seatAndRoomService.reserveSeat(req);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Seat " + req.seatNumber() + " reserved successfully (" + req.seatTier() + ")",
                "seatNumber", req.seatNumber(),
                "price", req.safePrice()
        ));
    }
}
