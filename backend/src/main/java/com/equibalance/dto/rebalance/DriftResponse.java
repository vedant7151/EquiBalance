package com.equibalance.dto.rebalance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriftResponse {
    private Long portfolioId;
    private BigDecimal totalValue;
    private boolean needsRebalancing;
    private BigDecimal rebalanceThresholdPct;
    private List<DriftItem> items;
}
