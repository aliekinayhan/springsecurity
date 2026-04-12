package com.ayhanekin.SpringSecurityBackend.service;

import com.ayhanekin.SpringSecurityBackend.dto.request.LoginRequest;
import com.ayhanekin.SpringSecurityBackend.dto.request.RefreshRequest;
import com.ayhanekin.SpringSecurityBackend.dto.request.RegisterRequest;
import com.ayhanekin.SpringSecurityBackend.entity.RefreshToken;
import com.ayhanekin.SpringSecurityBackend.entity.Role;
import com.ayhanekin.SpringSecurityBackend.entity.User;
import com.ayhanekin.SpringSecurityBackend.repository.UserRepository;
import com.ayhanekin.SpringSecurityBackend.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.ayhanekin.SpringSecurityBackend.dto.response.AuthResponse;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    public AuthService(
            UserRepository repository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService

    ) {
        this.userRepository = repository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public String register(RegisterRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(user);
        return "User Created...";
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        String accessToken = jwtService.generateToken(request.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
        String accessToken = jwtService.generateToken(refreshToken.getUser().getUsername());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    public String logout(String username) {
        refreshTokenService.deleteRefreshToken(username);
        return "Logged out successfully";
    }
}


