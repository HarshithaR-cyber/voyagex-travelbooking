package travelbooking.dto;

import java.time.LocalDate;
import java.util.Set;

public record UserDto(
        Long id,
        String fullName,
        String email,
        String membershipTier,
        String initials,
        Set<String> savedDealIds,
        LocalDate createdDate
) {}
