package com.equibalance.dto.analytics;

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
public class AnalyticsResponse {
    private BigDecimal totalValue;
    private BigDecimal dayGainLoss;
    private BigDecimal dayGainLossPct;
    private BigDecimal overallReturnPct;
    private List<AllocationSlice> currentAllocation;
    private List<AllocationSlice> targetAllocation;
    private List<ValuePoint> performanceHistory;
    private List<RebalanceHistoryItem> rebalanceHistory;
}
