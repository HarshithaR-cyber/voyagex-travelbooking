package travelbooking.model;

import java.util.List;

/**
 * Individual seat in an aircraft cabin layout.
 */
public record SeatInfo(
        String seatNumber,
        int row,
        String column,
        String tier, // Standard, Preferred, Extra Legroom
        int price,
        boolean available,
        List<String> features // Window, Aisle, Exit Row, Power Outlet
) {}
