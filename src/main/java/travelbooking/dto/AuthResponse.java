package travelbooking.dto;

public record AuthResponse(
        boolean success,
        String message,
        String token,
        UserDto user
) {}
