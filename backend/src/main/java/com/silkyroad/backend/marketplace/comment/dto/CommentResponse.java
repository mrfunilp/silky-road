package com.silkyroad.backend.marketplace.comment.dto;

import com.silkyroad.backend.user.dto.UserSummaryResponse;
import java.time.Instant;
import java.util.UUID;

public record CommentResponse(UUID id, UserSummaryResponse author, String text, Instant createdAt) {}
