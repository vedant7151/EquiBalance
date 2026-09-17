package com.equibalance.controller;

import com.equibalance.dto.rebalance.DriftResponse;
import com.equibalance.dto.rebalance.RebalanceResponse;
import com.equibalance.security.CurrentUserProvider;
import com.equibalance.service.DriftService;
import com.equibalance.service.RebalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}")
@RequiredArgsConstructor
public class RebalanceController {

    private final DriftService driftService;
    private final RebalanceService rebalanceService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/drift")
    public DriftResponse drift(@PathVariable Long portfolioId) {
        Long userId = currentUserProvider.getCurrentUserId();
        return driftService.calculateDrift(portfolioId, userId);
    }

    @PostMapping("/rebalance")
    public RebalanceResponse rebalance(@PathVariable Long portfolioId) {
        Long userId = currentUserProvider.getCurrentUserId();
        return rebalanceService.generateRebalance(portfolioId, userId);
    }
}
