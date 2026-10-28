package travelbooking.dto;

import java.time.LocalDate;

/**
 * Form backing object for flight search queries.
 */
public class FlightSearchRequest {

    private String tripType = "roundTrip";
    private String origin = "New York (JFK)";
    private String destination = "Paris (CDG)";
    private LocalDate departureDate = LocalDate.now().plusDays(3);
    private LocalDate returnDate = LocalDate.now().plusDays(10);
    private int adults = 1;
    private int children = 0;
    private String cabinClass = "Economy";

    public FlightSearchRequest() {
    }

    public FlightSearchRequest(String tripType, String origin, String destination,
                               LocalDate departureDate, LocalDate returnDate,
                               int adults, int children, String cabinClass) {
        this.tripType = tripType;
        this.origin = origin;
        this.destination = destination;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.adults = adults;
        this.children = children;
        this.cabinClass = cabinClass;
    }

    public String getTripType() {
        return tripType;
    }

    public void setTripType(String tripType) {
        this.tripType = tripType;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public int getAdults() {
        return adults;
    }

    public void setAdults(int adults) {
        this.adults = adults;
    }

    public int getChildren() {
        return children;
    }

    public void setChildren(int children) {
        this.children = children;
    }

    public String getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(String cabinClass) {
        this.cabinClass = cabinClass;
    }

    public int getTotalPassengers() {
        return adults + children;
    }
}
