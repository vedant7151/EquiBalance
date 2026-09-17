package com.equibalance.controller;

import com.equibalance.dto.portfolio.PortfolioRequest;
import com.equibalance.dto.portfolio.PortfolioResponse;
import com.equibalance.security.CurrentUserProvider;
import com.equibalance.service.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<PortfolioResponse> create(@Valid @RequestBody PortfolioRequest request) {
        var user = currentUserProvider.getCurrentUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioService.create(user, request));
    }

    @GetMapping
    public ResponseEntity<List<PortfolioResponse>> list() {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(portfolioService.listForUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PortfolioResponse> get(@PathVariable Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(portfolioService.getForUser(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PortfolioResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody PortfolioRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(portfolioService.update(id, userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        portfolioService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
