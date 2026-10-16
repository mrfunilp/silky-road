package com.silkyroad.backend.marketplace;

import com.silkyroad.backend.marketplace.dto.CreateMarketplaceItemRequest;
import com.silkyroad.backend.marketplace.dto.MarketplaceItemResponse;
import com.silkyroad.backend.marketplace.dto.UpdateMarketplaceItemRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/items")
public class MarketplaceItemController {

    private final MarketplaceItemService itemService;

    public MarketplaceItemController(MarketplaceItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<MarketplaceItemResponse> findAll(
            @RequestParam(required = false) UUID creatorId,
            @RequestParam(required = false) String status) {
        return itemService.findAll(creatorId, status);
    }

    @GetMapping("/{id}")
    public MarketplaceItemResponse findById(@PathVariable UUID id) {
        return itemService.findById(id);
    }

    @PostMapping
    public ResponseEntity<MarketplaceItemResponse> create(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateMarketplaceItemRequest request) {
        MarketplaceItemResponse created = itemService.create(request, callerId(jwt));
        return ResponseEntity.created(URI.create("/api/items/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public MarketplaceItemResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMarketplaceItemRequest request) {
        return itemService.update(id, request, callerId(jwt));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        itemService.delete(id, callerId(jwt));
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MarketplaceItemResponse addImage(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return itemService.addImage(id, file, callerId(jwt));
    }

    @DeleteMapping("/{id}/images/{name}")
    public MarketplaceItemResponse removeImage(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String name) {
        return itemService.removeImage(id, name, callerId(jwt));
    }

    private UUID callerId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
