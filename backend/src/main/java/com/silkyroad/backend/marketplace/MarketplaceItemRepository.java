package com.silkyroad.backend.marketplace;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketplaceItemRepository extends JpaRepository<MarketplaceItem, UUID> {

    List<MarketplaceItem> findAllByCreatorId(UUID creatorId);

    List<MarketplaceItem> findAllByStatus(MarketplaceItemStatus status);
}
