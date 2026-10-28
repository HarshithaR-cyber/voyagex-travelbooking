package travelbooking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record NewsletterSubscriptionRequest(
        @NotBlank(message = "Email address is required.")
        @Email(message = "Please enter a valid email address.")
        String email
) {}
