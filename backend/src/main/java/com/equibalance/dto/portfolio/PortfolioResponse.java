package com.equibalance.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioResponse {
    private Long id;
    private String name;
    private String description;
    private String baseCurrency;
    private BigDecimal rebalanceThresholdPct;
    private Instant createdAt;
    private BigDecimal totalValue;
    private boolean needsRebalancing;
    private List<HoldingResponse> holdings;
}
