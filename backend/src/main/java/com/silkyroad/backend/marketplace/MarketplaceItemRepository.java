package com.silkyroad.backend.marketplace;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface MarketplaceItemRepository extends JpaRepository<MarketplaceItem, UUID> {

    List<MarketplaceItem> findAllByCreatorId(UUID creatorId);

    List<MarketplaceItem> findAllByStatus(MarketplaceItemStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from MarketplaceItem i where i.id = :id")
    Optional<MarketplaceItem> findByIdForUpdate(UUID id);
}
