package travelbooking.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import travelbooking.dto.*;
import travelbooking.service.FlightSearchService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flights")
@CrossOrigin(origins = "*")
public class FlightApiController {

    private final FlightSearchService flightSearchService;

    public FlightApiController(FlightSearchService flightSearchService) {
        this.flightSearchService = flightSearchService;
    }

    /**
     * Endpoint to retrieve popular Indian and international airport destinations.
     */
    @GetMapping("/airports")
    public ResponseEntity<List<AirportDto>> getAirports() {
        return ResponseEntity.ok(flightSearchService.getAllAirports());
    }

    /**
     * Endpoint to search for flight options with validation and filtering.
     * Example: GET /api/flights/search?origin=DEL&destination=DXB&departDate=2026-11-01&passengers=2
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchFlights(
            @RequestParam(value = "origin", required = false) String origin,
            @RequestParam(value = "destination", required = false) String destination,
            @RequestParam(value = "departDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departDate,
            @RequestParam(value = "departureDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
            @RequestParam(value = "returnDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
            @RequestParam(value = "passengers", defaultValue = "1") int passengers,
            @RequestParam(value = "tripType", defaultValue = "roundTrip") String tripType,
            @RequestParam(value = "cabinClass", defaultValue = "Economy") String cabinClass,
            @RequestParam(value = "airline", required = false) String airline,
            @RequestParam(value = "nonStop", required = false) Boolean nonStop,
            @RequestParam(value = "nonStopOnly", required = false) Boolean nonStopOnly,
            @RequestParam(value = "maxPrice", required = false) Integer maxPrice,
            @RequestParam(value = "sortBy", defaultValue = "price_asc") String sortBy
    ) {
        LocalDate resolvedDepartDate = departDate != null ? departDate : departureDate;
        Boolean resolvedNonStop = nonStop != null ? nonStop : nonStopOnly;
        FlightSearchCriteria criteria = new FlightSearchCriteria(
                origin, destination, resolvedDepartDate, returnDate,
                passengers, tripType, cabinClass, airline, resolvedNonStop, maxPrice, sortBy
        );

        Map<String, String> errors = flightSearchService.validateCriteria(criteria);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Flight search validation failed",
                    "errors", errors
            ));
        }

        FlightSearchResponse response = flightSearchService.searchFlights(criteria);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to confirm a simulated flight reservation.
     */
    @PostMapping("/book")
    public ResponseEntity<?> confirmBooking(@RequestBody BookingRequest request) {
        if (request.passengerName() == null || request.passengerName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Passenger name is required for reservation."
            ));
        }
        if (request.passengerEmail() == null || !request.passengerEmail().contains("@")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "A valid email address is required for e-ticket delivery."
            ));
        }

        BookingResponse response = flightSearchService.createBooking(request);
        return ResponseEntity.ok(response);
    }
}
