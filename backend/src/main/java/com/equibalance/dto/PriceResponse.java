package com.equibalance.dto;

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
public class PriceResponse {
    private String ticker;
    private BigDecimal price;
    private String currency;
    private Instant fetchedAt;
    private boolean stale;
}
