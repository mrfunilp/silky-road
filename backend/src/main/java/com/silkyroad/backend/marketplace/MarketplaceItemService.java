package com.silkyroad.backend.marketplace;

import com.silkyroad.backend.common.ForbiddenException;
import com.silkyroad.backend.common.NotFoundException;
import com.silkyroad.backend.marketplace.dto.CreateMarketplaceItemRequest;
import com.silkyroad.backend.marketplace.dto.MarketplaceItemResponse;
import com.silkyroad.backend.marketplace.dto.UpdateMarketplaceItemRequest;
import com.silkyroad.backend.user.User;
import com.silkyroad.backend.user.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MarketplaceItemService {

    private final MarketplaceItemRepository itemRepository;
    private final MarketplaceItemMapper itemMapper;
    private final UserService userService;

    public MarketplaceItemService(
            MarketplaceItemRepository itemRepository,
            MarketplaceItemMapper itemMapper,
            UserService userService) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
        this.userService = userService;
    }

    public List<MarketplaceItemResponse> findAll(UUID creatorId, String status) {
        List<MarketplaceItem> items;
        if (creatorId != null) {
            items = itemRepository.findAllByCreatorId(creatorId);
        } else if (status != null && !status.isBlank()) {
            items = itemRepository.findAllByStatus(MarketplaceItemStatus.fromValue(status));
        } else {
            items = itemRepository.findAll();
        }
        return items.stream().map(itemMapper::toResponse).toList();
    }

    public MarketplaceItemResponse findById(UUID id) {
        return itemMapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public MarketplaceItemResponse create(CreateMarketplaceItemRequest request, UUID creatorId) {
        User creator = userService.getOrThrow(creatorId);
        MarketplaceItem saved = itemRepository.save(itemMapper.toEntity(request, creator));
        return itemMapper.toResponse(saved);
    }

    @Transactional
    public MarketplaceItemResponse update(UUID id, UpdateMarketplaceItemRequest request, UUID actingUserId) {
        MarketplaceItem item = getOrThrow(id);
        requireOwner(item, actingUserId);
        itemMapper.applyUpdate(item, request);
        return itemMapper.toResponse(item);
    }

    @Transactional
    public void delete(UUID id, UUID actingUserId) {
        MarketplaceItem item = getOrThrow(id);
        requireOwner(item, actingUserId);
        itemRepository.delete(item);
    }

    private void requireOwner(MarketplaceItem item, UUID actingUserId) {
        if (!item.getCreator().getId().equals(actingUserId)) {
            throw new ForbiddenException("That listing belongs to someone else.");
        }
    }

    private MarketplaceItem getOrThrow(UUID id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No marketplace item with id " + id));
    }
}
