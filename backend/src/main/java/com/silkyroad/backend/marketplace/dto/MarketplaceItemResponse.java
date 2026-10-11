package com.silkyroad.backend.marketplace.dto;

import com.silkyroad.backend.user.dto.UserSummaryResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MarketplaceItemResponse(
        UUID id,
        String name,
        String biography,
        UserSummaryResponse creator,
        long likes,
        long views,
        BigDecimal price,
        String currency,
        String category,
        List<String> tags,
        List<String> imageUrls,
        String status,
        Instant createdAt,
        Instant updatedAt) {}
