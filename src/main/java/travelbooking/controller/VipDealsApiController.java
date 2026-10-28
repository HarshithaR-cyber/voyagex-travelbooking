package travelbooking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import travelbooking.model.VipDeal;
import travelbooking.service.VipDealsService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deals")
@CrossOrigin(origins = "*")
public class VipDealsApiController {

    private final VipDealsService vipDealsService;

    public VipDealsApiController(VipDealsService vipDealsService) {
        this.vipDealsService = vipDealsService;
    }

    @GetMapping
    public ResponseEntity<List<VipDeal>> getDeals(
            @RequestParam(required = false, defaultValue = "ALL") String category
    ) {
        return ResponseEntity.ok(vipDealsService.getDeals(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VipDeal> getDealById(@PathVariable String id) {
        return vipDealsService.getDealById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<Map<String, Object>> validateDeal(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, Integer> body
    ) {
        int travelers = (body != null && body.containsKey("travelers")) ? body.get("travelers") : 1;
        Map<String, Object> quote = vipDealsService.validateAndQuoteDeal(id, travelers);
        if (Boolean.FALSE.equals(quote.get("valid"))) {
            return ResponseEntity.badRequest().body(quote);
        }
        return ResponseEntity.ok(quote);
    }
}
