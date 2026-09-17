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
public class TransactionResponse {
    private Long id;
    private String ticker;
    private String type;
    private BigDecimal units;
    private BigDecimal priceAtTime;
    private Instant executedAt;
}
