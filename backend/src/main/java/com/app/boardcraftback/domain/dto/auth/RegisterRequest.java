package com.app.boardcraftback.domain.dto.auth;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 2, max = 50) String nickname,
        @NotBlank @Size(min = 8, max = 72) String password,

        @AssertTrue(message = "약관에 동의해야 가입할 수 있습니다.")
        boolean terms
) {
}
