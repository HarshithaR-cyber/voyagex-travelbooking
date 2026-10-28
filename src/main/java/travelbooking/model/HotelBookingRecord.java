package travelbooking.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Record of a confirmed hotel reservation.
 */
public record HotelBookingRecord(
        String bookingReference,
        String hotelId,
        String hotelName,
        String roomId,
        String roomName,
        String checkInDate,
        String checkOutDate,
        int nights,
        int guests,
        int totalPaid,
        String guestName,
        String guestEmail,
        String status
) {
    @JsonProperty("confirmationCode")
    public String confirmationCode() {
        return bookingReference;
    }
}
