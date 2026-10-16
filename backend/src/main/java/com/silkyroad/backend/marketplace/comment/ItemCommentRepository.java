package com.silkyroad.backend.marketplace.comment;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemCommentRepository extends JpaRepository<ItemComment, UUID> {

    List<ItemComment> findAllByItemIdOrderByCreatedAtAsc(UUID itemId);

    void deleteAllByItemId(UUID itemId);
}
