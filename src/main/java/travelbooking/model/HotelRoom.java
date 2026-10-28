package travelbooking.model;

import java.util.List;

/**
 * Room category and details for hotel stays.
 */
public record HotelRoom(
        String roomId,
        String hotelId,
        String roomName,
        String bedType,
        int sizeSqm,
        int pricePerNight,
        int availableCount,
        List<String> amenities,
        String imageUrl
) {}
