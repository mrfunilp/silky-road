package com.silkyroad.backend.user.dto;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(UUID id, String username, String email, Instant createdAt) {}
