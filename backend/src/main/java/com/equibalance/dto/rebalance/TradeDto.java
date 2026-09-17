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
public class TradeDto {
    private String ticker;
    private String action; // BUY | SELL
    private BigDecimal units;
    private BigDecimal estimatedPrice;
    private BigDecimal estimatedCost; // positive amount, direction implied by action
}
