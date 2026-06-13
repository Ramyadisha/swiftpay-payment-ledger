package com.swiftpay.gateway.service;

import com.swiftpay.gateway.dto.AuthRequest;
import com.swiftpay.gateway.dto.AuthResponse;
import com.swiftpay.gateway.repository.AccountRepository;
import com.swiftpay.gateway.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AccountRepository accountRepository;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public AuthResponse login(AuthRequest request) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "password";

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        String token = jwtTokenProvider.generateToken(auth);
        var account = accountRepository.findByUsername(request.getUsername()).orElseThrow();
        return AuthResponse.builder()
                .accessToken(token).tokenType("Bearer")
                .expiresIn(expirationMs / 1000)
                .username(account.getUsername()).role(account.getRole())
                .build();
    }
}