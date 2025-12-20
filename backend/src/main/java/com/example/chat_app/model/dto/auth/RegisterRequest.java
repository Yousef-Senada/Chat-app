package com.example.chat_app.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Name is required") String name,

        @NotBlank(message = "Username is required") @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters") String username,

        @NotBlank(message = "Phone number is required") @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be 10-15 digits") String phoneNumber,

        @NotBlank(message = "Password is required") @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", message = "Password must contain at least one uppercase, one lowercase, one digit, and one special character (@$!%*?&)") String password) {
    /**
     * Compact constructor to trim all string fields.
     */
    public RegisterRequest {
        name = name != null ? name.trim() : null;
        username = username != null ? username.trim() : null;
        phoneNumber = phoneNumber != null ? phoneNumber.trim() : null;
        password = password != null ? password.trim() : null;
    }
}
