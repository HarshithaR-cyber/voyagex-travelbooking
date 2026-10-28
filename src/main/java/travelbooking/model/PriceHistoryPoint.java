package travelbooking.model;

/**
 * Historical/predicted price point for route dynamic pricing trends.
 */
public record PriceHistoryPoint(
        String date,
        int price,
        String trend // LOW, TYPICAL, SURGE
) {}
