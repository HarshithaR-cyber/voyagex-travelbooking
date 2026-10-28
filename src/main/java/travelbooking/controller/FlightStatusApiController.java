package travelbooking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import travelbooking.model.FlightStatusInfo;
import travelbooking.service.FlightStatusService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flight-status")
@CrossOrigin(origins = "*")
public class FlightStatusApiController {

    private final FlightStatusService flightStatusService;

    public FlightStatusApiController(FlightStatusService flightStatusService) {
        this.flightStatusService = flightStatusService;
    }

    @GetMapping
    public ResponseEntity<List<FlightStatusInfo>> getAllFlightStatuses() {
        return ResponseEntity.ok(flightStatusService.getAllStatuses());
    }

    @GetMapping("/{flightNumber}")
    public ResponseEntity<?> getFlightStatus(@PathVariable String flightNumber) {
        return flightStatusService.getStatusByFlightNumber(flightNumber)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(
                        (FlightStatusInfo) null
                ));
    }

    @PostMapping("/notify")
    public ResponseEntity<Map<String, Object>> subscribeNotifications(@RequestBody Map<String, String> body) {
        String flightNumber = body.get("flightNumber");
        String contact = body.get("contact");
        String type = body.get("type");

        if (flightNumber == null || contact == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Flight number and contact are required."
            ));
        }

        flightStatusService.subscribeNotifications(flightNumber, contact, type);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Subscribed to live status updates for " + flightNumber + " (" + contact + ")"
        ));
    }
}
