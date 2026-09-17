package com.equibalance.controller;

import com.equibalance.dto.analytics.AnalyticsResponse;
import com.equibalance.security.CurrentUserProvider;
import com.equibalance.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public AnalyticsResponse getAnalytics(@PathVariable Long portfolioId) {
        Long userId = currentUserProvider.getCurrentUserId();
        return analyticsService.getAnalytics(portfolioId, userId);
    }
}
