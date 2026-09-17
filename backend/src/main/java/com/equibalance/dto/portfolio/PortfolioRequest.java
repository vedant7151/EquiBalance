package com.equibalance.dto.portfolio;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PortfolioRequest {

    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 500)
    private String description;

    @Size(min = 3, max = 3)
    private String baseCurrency = "USD";

    @DecimalMin(value = "0.5")
    @DecimalMax(value = "50.0")
    private BigDecimal rebalanceThresholdPct = new BigDecimal("5.00");
}
