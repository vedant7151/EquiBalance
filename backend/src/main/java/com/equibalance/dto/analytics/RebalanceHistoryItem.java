package com.equibalance.dto.analytics;

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
public class RebalanceHistoryItem {
    private Long id;
    private Instant triggeredAt;
    private BigDecimal totalDriftBefore;
    private String status;
    private int tradesCount;
}
