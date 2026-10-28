package travelbooking.dto;

public record PriceFreezeRequest(
        String flightNumber,
        String airline,
        String route,
        String departureDate,
        Integer fare,
        String travelerEmail
) {
    public int safeFare() {
        return fare != null ? fare : 250;
    }
}
