package travelbooking.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Record of an executed cancellation and issued refund reference.
 */
public record RefundRecord(
        String refundReference,
        String bookingReference,
        String travelerEmail,
        String cancellationReason,
        int originalAmount,
        int refundAmount,
        int cancellationFee,
        String status, // PENDING_REVIEW, APPROVED, REFUNDED
        String requestDate,
        String estimatedSettlementDate,
        String paymentMethodSummary
) {
    @JsonProperty("netRefundAmount")
    public int netRefundAmount() {
        return refundAmount;
    }
}
