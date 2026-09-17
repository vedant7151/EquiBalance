package com.equibalance.dto.portfolio;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HoldingRequest {

    @NotBlank
    private String ticker;

    @NotNull
    @DecimalMin(value = "0.000001")
    private BigDecimal units;

    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private BigDecimal targetAllocationPct;
}
