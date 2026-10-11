package com.silkyroad.backend.marketplace.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record CreateMarketplaceItemRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 2000) String biography,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @Size(max = 60) String category,
        List<String> tags,
        List<String> imageUrls,
        String status) {}
