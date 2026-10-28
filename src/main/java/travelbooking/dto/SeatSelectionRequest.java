package travelbooking.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class SeatSelectionRequest {
    private String bookingReference;
    private String flightNumber;

    @JsonAlias({"seatCode", "seatNumber", "seat"})
    private String seatNumber;

    @JsonAlias({"tier", "seatTier"})
    private String seatTier;

    @JsonAlias({"price", "seatPrice"})
    private Integer seatPrice;

    private String passengerName;

    public SeatSelectionRequest() {}

    public SeatSelectionRequest(String bookingReference, String flightNumber, String seatNumber, String seatTier, Integer seatPrice, String passengerName) {
        this.bookingReference = bookingReference;
        this.flightNumber = flightNumber;
        this.seatNumber = seatNumber;
        this.seatTier = seatTier;
        this.seatPrice = seatPrice;
        this.passengerName = passengerName;
    }

    public String bookingReference() { return bookingReference; }
    public String flightNumber() { return flightNumber; }
    public String seatNumber() { return seatNumber; }
    public String seatTier() { return seatTier; }
    public Integer seatPrice() { return seatPrice; }
    public String passengerName() { return passengerName; }

    public int safePrice() {
        return seatPrice != null ? seatPrice : 0;
    }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public String getSeatTier() { return seatTier; }
    public void setSeatTier(String seatTier) { this.seatTier = seatTier; }
    public Integer getSeatPrice() { return seatPrice; }
    public void setSeatPrice(Integer seatPrice) { this.seatPrice = seatPrice; }
    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
}
