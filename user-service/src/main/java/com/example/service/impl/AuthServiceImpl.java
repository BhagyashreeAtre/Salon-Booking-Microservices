package com.example.service.impl;

import com.example.model.User;
import com.example.payload.dto.AuthResponse;
import com.example.payload.dto.SignupDTO;
import com.example.payload.dto.TokenResponse;
import com.example.repository.UserRepository;
import com.example.service.AuthService;
import com.example.service.Keycloakservice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final Keycloakservice keycloakservice;
    @Override
    public AuthResponse login(String email, String password) throws Exception {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("Invalid email or password");
        }

        TokenResponse tokenResponse = keycloakservice.getUserAccessToken(
                user.getUsername(),
                password,
                "password",
                null
        );

        AuthResponse authResponse = new AuthResponse();
        authResponse.setRefresh_token(tokenResponse.getRefreshToken());
        authResponse.setJwt(tokenResponse.getAccessToken());
        authResponse.setRole(user.getRole());
        authResponse.setMessage("Login success");

        return authResponse;
    }
    @Override
    public AuthResponse signup(SignupDTO req) throws Exception {
        keycloakservice.createUser(req);

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setRole(req.getRole());
        user.setFullName(req.getFullName());
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        TokenResponse tokenResponse = keycloakservice.getUserAccessToken(req.getUsername(), req.getPassword(), "password", null);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setRefresh_token(tokenResponse.getRefreshToken());
        authResponse.setJwt(tokenResponse.getAccessToken());
        authResponse.setRole(user.getRole());
        authResponse.setMessage("Register success");

        return authResponse;
    }

    @Override
    public AuthResponse getAccessTokenFromRefreshToken(String refreshToken) throws Exception {
        TokenResponse tokenResponse=keycloakservice.getUserAccessToken(null, null, "refresh_token", refreshToken);
        AuthResponse authResponse=new AuthResponse();
        authResponse.setRefresh_token(tokenResponse.getRefreshToken());
        authResponse.setJwt(tokenResponse.getAccessToken());
        authResponse.setMessage("Register success");

        return authResponse;
    }
}
