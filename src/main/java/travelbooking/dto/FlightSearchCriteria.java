package travelbooking.dto;

import java.time.LocalDate;

/**
 * Filter and query parameters for flight searches.
 */
public class FlightSearchCriteria {

    private String origin;
    private String destination;
    private LocalDate departDate;
    private LocalDate returnDate;
    private int passengers = 1;
    private String tripType = "roundTrip";
    private String cabinClass = "Economy";
    private String airline;
    private Boolean nonStopOnly = false;
    private Integer maxPrice;
    private String sortBy = "price_asc";

    public FlightSearchCriteria() {}

    public FlightSearchCriteria(String origin, String destination, LocalDate departDate,
                                LocalDate returnDate, int passengers, String tripType,
                                String cabinClass, String airline, Boolean nonStopOnly,
                                Integer maxPrice, String sortBy) {
        this.origin = origin;
        this.destination = destination;
        this.departDate = departDate;
        this.returnDate = returnDate;
        this.passengers = passengers > 0 ? passengers : 1;
        this.tripType = tripType != null ? tripType : "roundTrip";
        this.cabinClass = cabinClass != null ? cabinClass : "Economy";
        this.airline = airline;
        this.nonStopOnly = nonStopOnly != null && nonStopOnly;
        this.maxPrice = maxPrice;
        this.sortBy = sortBy != null ? sortBy : "price_asc";
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

    public LocalDate getDepartDate() {
        return departDate;
    }

    public void setDepartDate(LocalDate departDate) {
        this.departDate = departDate;
    }

    public LocalDate getDepartureDate() {
        return departDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departDate = departureDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public int getPassengers() {
        return passengers;
    }

    public void setPassengers(int passengers) {
        this.passengers = passengers;
    }

    public String getTripType() {
        return tripType;
    }

    public void setTripType(String tripType) {
        this.tripType = tripType;
    }

    public String getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(String cabinClass) {
        this.cabinClass = cabinClass;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public Boolean getNonStopOnly() {
        return nonStopOnly;
    }

    public void setNonStopOnly(Boolean nonStopOnly) {
        this.nonStopOnly = nonStopOnly;
    }

    public Boolean getNonStop() {
        return nonStopOnly;
    }

    public void setNonStop(Boolean nonStop) {
        this.nonStopOnly = nonStop;
    }

    public Integer getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Integer maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
}
