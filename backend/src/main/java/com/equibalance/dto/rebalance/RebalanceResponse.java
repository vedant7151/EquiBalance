package com.equibalance.dto.rebalance;

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
public class RebalanceResponse {
    private Long portfolioId;
    private Instant triggeredAt;
    private BigDecimal totalDriftBefore;
    private List<TradeDto> trades;
}
