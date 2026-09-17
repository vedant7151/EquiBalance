package com.equibalance.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldingResponse {
    private Long id;
    private String ticker;
    private BigDecimal units;
    private BigDecimal targetAllocationPct;
    private BigDecimal currentPrice;
    private boolean stale;
    private BigDecimal currentValue;
    private BigDecimal currentAllocationPct;
    private BigDecimal driftPct;
    private Instant addedAt;
}
