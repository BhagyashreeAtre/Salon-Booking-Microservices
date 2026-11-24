package com.example.service;

import com.example.payload.dto.AuthResponse;
import com.example.payload.dto.SignupDTO;

public interface AuthService {
    AuthResponse login(String username,String password) throws Exception;
    AuthResponse signup(SignupDTO req) throws Exception;
    AuthResponse getAccessTokenFromRefreshToken(String refreshToken) throws Exception;
}
