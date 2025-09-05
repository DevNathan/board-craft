package com.app.boardcraftback.api.v1.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 2, max = 50) String nickname,
        @NotBlank @Size(min = 8, max = 72) String password,
        boolean terms
) {
}
