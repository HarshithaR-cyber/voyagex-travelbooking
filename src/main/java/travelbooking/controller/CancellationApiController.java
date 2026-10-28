package travelbooking.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import travelbooking.dto.CancellationRequest;
import travelbooking.model.CancellationPolicyQuote;
import travelbooking.model.RefundRecord;
import travelbooking.service.CancellationRefundService;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/cancellations")
@CrossOrigin(origins = "*")
public class CancellationApiController {

    private final CancellationRefundService cancellationRefundService;

    public CancellationApiController(CancellationRefundService cancellationRefundService) {
        this.cancellationRefundService = cancellationRefundService;
    }

    @GetMapping("/policy")
    public ResponseEntity<CancellationPolicyQuote> getPolicyQuote(
            @RequestParam(required = false, defaultValue = "VX-SAMPLE") String bookingReference,
            @RequestParam(required = false, defaultValue = "300") int originalFare,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
            @RequestParam(required = false, defaultValue = "false") boolean medicalWaiver
    ) {
        return ResponseEntity.ok(cancellationRefundService.calculateRefundQuote(
                bookingReference, originalFare, departureDate, medicalWaiver
        ));
    }

    @PostMapping("/request")
    public ResponseEntity<RefundRecord> processCancellation(
            @RequestBody CancellationRequest req,
            @RequestParam(required = false) Integer originalFare,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate
    ) {
        int effectiveFare = req.originalFare() != null && req.originalFare() > 0 ? req.originalFare() : (originalFare != null ? originalFare : 300);
        return ResponseEntity.ok(cancellationRefundService.processCancellation(req, effectiveFare, departureDate));
    }

    @GetMapping("/{reference}")
    public ResponseEntity<?> getRefundStatus(@PathVariable String reference) {
        return cancellationRefundService.getRefundRecord(reference)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(
                        (RefundRecord) null
                ));
    }
}
