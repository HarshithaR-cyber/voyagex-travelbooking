package travelbooking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import travelbooking.dto.PriceFreezeRequest;
import travelbooking.model.PriceFreezeQuote;
import travelbooking.model.PriceHistoryPoint;
import travelbooking.service.DynamicPricingService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pricing")
@CrossOrigin(origins = "*")
public class PricingApiController {

    private final DynamicPricingService dynamicPricingService;

    public PricingApiController(DynamicPricingService dynamicPricingService) {
        this.dynamicPricingService = dynamicPricingService;
    }

    @GetMapping("/history")
    public ResponseEntity<List<PriceHistoryPoint>> getPriceHistory(
            @RequestParam(required = false, defaultValue = "DEL") String origin,
            @RequestParam(required = false, defaultValue = "BOM") String destination,
            @RequestParam(required = false, defaultValue = "150") int basePrice
    ) {
        return ResponseEntity.ok(dynamicPricingService.getPriceHistory(origin, destination, basePrice));
    }

    @PostMapping("/freeze")
    public ResponseEntity<PriceFreezeQuote> freezePrice(@RequestBody PriceFreezeRequest req) {
        return ResponseEntity.ok(dynamicPricingService.freezePrice(req));
    }

    @GetMapping("/frozen/{token}")
    public ResponseEntity<?> getFrozenPrice(@PathVariable String token) {
        return dynamicPricingService.getFrozenQuote(token)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(
                        (PriceFreezeQuote) null
                ));
    }
}
