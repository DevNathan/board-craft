package com.app.boardcraftback.api.v1.auth;

import com.app.boardcraftback.api.v1.dto.auth.RegisterRequest;
import com.app.boardcraftback.api.v1.dto.auth.RegisterResponse;
import com.app.boardcraftback.service.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UsersService usersService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody RegisterRequest req) {
        var u = usersService.registerUser(req.email(), req.password(), req.nickname(), req.terms());
        return ResponseEntity
                .status(201)
                .body(new RegisterResponse(u.getId(), u.getEmail(), u.getNickname()));
    }
}
