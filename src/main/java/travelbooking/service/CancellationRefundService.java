package travelbooking.service;

import org.springframework.stereotype.Service;
import travelbooking.dto.CancellationRequest;
import travelbooking.model.CancellationPolicyQuote;
import travelbooking.model.RefundRecord;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CancellationRefundService {

    private final Map<String, RefundRecord> refundRecords = new ConcurrentHashMap<>();

    /**
     * Calculates itemized refund estimate according to airline fare rules and departure timing.
     */
    public CancellationPolicyQuote calculateRefundQuote(String bookingRef, int originalFare, LocalDate departureDate, boolean medicalWaiver) {
        int fare = originalFare > 0 ? originalFare : 300;
        long hours = 72; // default 3 days if date is not specified
        if (departureDate != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), departureDate);
            hours = Math.max(0, days * 24);
        }

        if (medicalWaiver) {
            return new CancellationPolicyQuote(
                    bookingRef, fare, hours,
                    "Compassionate Medical / Emergency Waiver",
                    100, 0, (int) Math.round(fare * 0.12),
                    (int) Math.round(fare * 1.12),
                    true,
                    "2-3 business days"
            );
        }

        int refundPct;
        int fee;
        String tier;

        if (hours >= 168) { // > 7 days
            refundPct = 90;
            fee = 35;
            tier = "Flexible Window (> 7 days prior): 90% base fare refunded";
        } else if (hours >= 48) { // 2 to 7 days
            refundPct = 70;
            fee = 60;
            tier = "Standard Window (48h – 7 days prior): 70% base fare refunded";
        } else if (hours >= 24) { // 24 to 48 hours
            refundPct = 50;
            fee = 85;
            tier = "Urgent Window (24h – 48h prior): 50% base fare refunded";
        } else { // < 24 hours
            refundPct = 25;
            fee = 110;
            tier = "Last-Minute Window (< 24h prior): 25% base fare refunded";
        }

        int refundableTaxes = (int) Math.round(fare * 0.12);
        int baseRefund = (int) Math.round((fare * refundPct) / 100.0);
        int netRefund = Math.max(0, baseRefund - fee + refundableTaxes);

        return new CancellationPolicyQuote(
                bookingRef, fare, hours, tier, refundPct, fee, refundableTaxes, netRefund, false, "3-5 business days"
        );
    }

    /**
     * Executes cancellation request and issues formal refund reference.
     */
    public RefundRecord processCancellation(CancellationRequest req, int originalFare, LocalDate departureDate) {
        CancellationPolicyQuote quote = calculateRefundQuote(
                req.bookingReference(), originalFare, departureDate, req.isMedicalWaiver()
        );

        String refNum = "RF-" + ThreadLocalRandom.current().nextInt(10000, 99999);
        String todayStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String estDelivery = LocalDate.now().plusDays(4).format(DateTimeFormatter.ISO_DATE);

        RefundRecord record = new RefundRecord(
                refNum,
                req.bookingReference() != null ? req.bookingReference() : "VX-BOOKING",
                req.travelerEmail() != null ? req.travelerEmail() : "traveler@example.com",
                req.getEffectiveReason(),
                quote.originalFare(),
                quote.netRefundAmount(),
                quote.cancellationFee(),
                "APPROVED",
                todayStr,
                estDelivery,
                "Original Payment Method (Demo Refund)"
        );

        refundRecords.put(refNum, record);
        if (req.bookingReference() != null) {
            refundRecords.put(req.bookingReference().toUpperCase(), record);
        }

        return record;
    }

    public Optional<RefundRecord> getRefundRecord(String reference) {
        if (reference == null) return Optional.empty();
        return Optional.ofNullable(refundRecords.get(reference.trim().toUpperCase()));
    }
}
