package com.ayhanekin.SpringSecurityBackend.controller;

import com.ayhanekin.SpringSecurityBackend.dto.request.LoginRequest;
import com.ayhanekin.SpringSecurityBackend.dto.request.RefreshRequest;
import com.ayhanekin.SpringSecurityBackend.dto.request.RegisterRequest;
import com.ayhanekin.SpringSecurityBackend.dto.response.AuthResponse;
import com.ayhanekin.SpringSecurityBackend.entity.RefreshToken;
import com.ayhanekin.SpringSecurityBackend.security.JwtService;
import com.ayhanekin.SpringSecurityBackend.service.AuthService;
import com.ayhanekin.SpringSecurityBackend.service.RefreshTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService service, RefreshTokenService refreshTokenService) {
        this.service = service;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(service.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
        service.logout(refreshToken.getUser().getUsername());
        return ResponseEntity.ok("Logged out successfully");
    }
}

