package travelbooking.dto;

public record CancellationRequest(
        String bookingReference,
        String travelerEmail,
        String reason,
        String cancellationReason,
        Integer originalFare,
        Boolean medicalWaiver,
        Boolean medicalEmergency
) {
    public String getEffectiveReason() {
        if (cancellationReason != null && !cancellationReason.isBlank()) {
            return cancellationReason;
        }
        return reason != null ? reason : "Travel plans altered";
    }

    public boolean isMedicalWaiver() {
        return Boolean.TRUE.equals(medicalWaiver) || Boolean.TRUE.equals(medicalEmergency);
    }
}
