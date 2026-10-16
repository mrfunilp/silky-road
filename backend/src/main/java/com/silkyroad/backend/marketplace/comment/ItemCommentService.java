package com.silkyroad.backend.marketplace.comment;

import com.silkyroad.backend.common.ForbiddenException;
import com.silkyroad.backend.common.NotFoundException;
import com.silkyroad.backend.marketplace.MarketplaceItem;
import com.silkyroad.backend.marketplace.MarketplaceItemRepository;
import com.silkyroad.backend.marketplace.comment.dto.CommentResponse;
import com.silkyroad.backend.marketplace.comment.dto.CreateCommentRequest;
import com.silkyroad.backend.user.User;
import com.silkyroad.backend.user.UserMapper;
import com.silkyroad.backend.user.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ItemCommentService {

    private final ItemCommentRepository commentRepository;
    private final MarketplaceItemRepository itemRepository;
    private final UserService userService;
    private final UserMapper userMapper;

    public ItemCommentService(
            ItemCommentRepository commentRepository,
            MarketplaceItemRepository itemRepository,
            UserService userService,
            UserMapper userMapper) {
        this.commentRepository = commentRepository;
        this.itemRepository = itemRepository;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    public List<CommentResponse> findAll(UUID itemId) {
        getItemOrThrow(itemId);
        return commentRepository.findAllByItemIdOrderByCreatedAtAsc(itemId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CommentResponse create(UUID itemId, CreateCommentRequest request, UUID authorId) {
        MarketplaceItem item = getItemOrThrow(itemId);
        if (item.getCreator().getId().equals(authorId)) {
            throw new ForbiddenException("You cannot comment on your own listing.");
        }
        User author = userService.getOrThrow(authorId);
        ItemComment saved = commentRepository.save(new ItemComment(item, author, request.text().trim()));
        return toResponse(saved);
    }

    @Transactional
    public void delete(UUID itemId, UUID commentId, UUID actingUserId) {
        ItemComment comment = commentRepository.findById(commentId)
                .filter(c -> c.getItem().getId().equals(itemId))
                .orElseThrow(() -> new NotFoundException("No comment with id " + commentId));
        if (!comment.getAuthor().getId().equals(actingUserId)) {
            throw new ForbiddenException("You can only delete your own comments.");
        }
        commentRepository.delete(comment);
    }

    private MarketplaceItem getItemOrThrow(UUID itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("No marketplace item with id " + itemId));
    }

    private CommentResponse toResponse(ItemComment comment) {
        return new CommentResponse(
                comment.getId(), userMapper.toSummary(comment.getAuthor()), comment.getText(), comment.getCreatedAt());
    }
}
