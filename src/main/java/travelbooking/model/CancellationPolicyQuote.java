package travelbooking.model;

/**
 * Calculated cancellation refund quote based on airline fare rules and departure timing.
 */
public record CancellationPolicyQuote(
        String bookingReference,
        int originalFare,
        long hoursUntilDeparture,
        String policyTier,
        int refundPercentage,
        int cancellationFee,
        int refundableTaxes,
        int netRefundAmount,
        boolean eligibleForWaiver,
        String estimatedProcessingTime
) {}
