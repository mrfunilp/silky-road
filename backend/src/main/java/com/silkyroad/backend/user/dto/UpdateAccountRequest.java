package com.silkyroad.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAccountRequest(
        @NotBlank @Size(min = 3, max = 20) String username,
        @NotBlank @Email String email,
        @NotBlank String currentPassword,
        @Size(min = 8) String newPassword) {}
