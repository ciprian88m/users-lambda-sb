package dev.ciprian.users.models;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.lang.Nullable;

public record User(
        @Nullable String firstName,
        @Nullable String lastName,
        @NotEmpty(message = "Email is required") String email,
        @NotEmpty(message = "Username is required") String username,
        @NotEmpty(message = "Password is required") String password) {
}
