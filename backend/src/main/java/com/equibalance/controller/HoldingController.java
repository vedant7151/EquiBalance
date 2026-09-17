package com.equibalance.controller;

import com.equibalance.dto.portfolio.HoldingRequest;
import com.equibalance.security.CurrentUserProvider;
import com.equibalance.service.HoldingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}/holdings")
@RequiredArgsConstructor
public class HoldingController {

    private final HoldingService holdingService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<Void> add(@PathVariable Long portfolioId, @Valid @RequestBody HoldingRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        holdingService.addHolding(portfolioId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{holdingId}")
    public ResponseEntity<Void> update(@PathVariable Long portfolioId, @PathVariable Long holdingId,
                                        @Valid @RequestBody HoldingRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        holdingService.updateHolding(portfolioId, holdingId, userId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{holdingId}")
    public ResponseEntity<Void> remove(@PathVariable Long portfolioId, @PathVariable Long holdingId) {
        Long userId = currentUserProvider.getCurrentUserId();
        holdingService.removeHolding(portfolioId, holdingId, userId);
        return ResponseEntity.noContent().build();
    }
}
