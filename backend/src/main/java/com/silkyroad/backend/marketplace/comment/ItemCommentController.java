package com.silkyroad.backend.marketplace.comment;

import com.silkyroad.backend.marketplace.comment.dto.CommentResponse;
import com.silkyroad.backend.marketplace.comment.dto.CreateCommentRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items/{itemId}/comments")
public class ItemCommentController {

    private final ItemCommentService commentService;

    public ItemCommentController(ItemCommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<CommentResponse> findAll(@PathVariable UUID itemId) {
        return commentService.findAll(itemId);
    }

    @PostMapping
    public ResponseEntity<CommentResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID itemId,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentResponse created = commentService.create(itemId, request, UUID.fromString(jwt.getSubject()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID itemId, @PathVariable UUID commentId) {
        commentService.delete(itemId, commentId, UUID.fromString(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }
}
