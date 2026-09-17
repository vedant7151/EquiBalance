package com.equibalance.dto.rebalance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriftItem {
    private String ticker;
    private BigDecimal units;
    private BigDecimal currentPrice;
    private BigDecimal currentValue;
    private BigDecimal currentPct;
    private BigDecimal targetPct;
    private BigDecimal driftPct; // currentPct - targetPct
}
