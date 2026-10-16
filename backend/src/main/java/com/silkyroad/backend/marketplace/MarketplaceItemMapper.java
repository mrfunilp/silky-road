package com.silkyroad.backend.marketplace;

import com.silkyroad.backend.marketplace.dto.CreateMarketplaceItemRequest;
import com.silkyroad.backend.marketplace.dto.MarketplaceItemResponse;
import com.silkyroad.backend.marketplace.dto.UpdateMarketplaceItemRequest;
import com.silkyroad.backend.user.User;
import com.silkyroad.backend.user.UserMapper;
import org.springframework.stereotype.Component;

@Component
public class MarketplaceItemMapper {

    private final UserMapper userMapper;

    public MarketplaceItemMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public MarketplaceItemResponse toResponse(MarketplaceItem item) {
        return new MarketplaceItemResponse(
                item.getId(),
                item.getName(),
                item.getBiography(),
                userMapper.toSummary(item.getCreator()),
                item.getPrice(),
                item.getCurrency(),
                item.getCategory(),
                item.getImageUrls().stream().map(name -> "/api/images/" + name).toList(),
                item.getStatus().toValue(),
                item.getCreatedAt());
    }

    public MarketplaceItem toEntity(CreateMarketplaceItemRequest request, User creator) {
        MarketplaceItem item = new MarketplaceItem(
                creator, request.name().trim(), request.price(), normaliseCurrency(request.currency()));
        item.setBiography(request.biography());
        item.setCategory(request.category());
        item.setStatus(MarketplaceItemStatus.fromValue(request.status()));
        return item;
    }

    public void applyUpdate(MarketplaceItem item, UpdateMarketplaceItemRequest request) {
        item.setName(request.name().trim());
        item.setBiography(request.biography());
        item.setPrice(request.price());
        item.setCurrency(normaliseCurrency(request.currency()));
        item.setCategory(request.category());
        item.setStatus(MarketplaceItemStatus.fromValue(request.status()));
    }

    private String normaliseCurrency(String currency) {
        return currency.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
