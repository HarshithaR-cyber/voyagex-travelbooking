package travelbooking.dto;

/**
 * Data transfer object representing an airport destination.
 */
public record AirportDto(
        String code,
        String city,
        String country,
        String name,
        boolean isIndian
) {}
