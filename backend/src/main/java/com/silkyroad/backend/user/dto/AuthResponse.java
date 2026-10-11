package com.silkyroad.backend.user.dto;

public record AuthResponse(String token, long expiresIn, UserResponse user) {}
