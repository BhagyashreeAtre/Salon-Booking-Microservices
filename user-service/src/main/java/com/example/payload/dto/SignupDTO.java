package com.example.payload.dto;

import com.example.domain.UserRole;
import lombok.Data;

@Data
public class SignupDTO{
    private String fullName;
    private String email;
    private String password;
    private String username;
    private UserRole role;
}
