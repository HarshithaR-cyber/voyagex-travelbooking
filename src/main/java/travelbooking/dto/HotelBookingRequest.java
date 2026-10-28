package travelbooking.dto;

public record HotelBookingRequest(
        String hotelId,
        String hotelName,
        String roomId,
        String roomName,
        String checkInDate,
        String checkOutDate,
        Integer nights,
        Integer guests,
        Integer totalAmount,
        String guestName,
        String guestEmail,
        String guestPhone
) {
    public int safeNights() { return nights != null && nights > 0 ? nights : 1; }
    public int safeGuests() { return guests != null && guests > 0 ? guests : 1; }
    public int safeTotalAmount() { return totalAmount != null ? totalAmount : 320; }
}
