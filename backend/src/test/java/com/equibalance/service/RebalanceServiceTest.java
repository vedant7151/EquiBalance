package com.equibalance.service;

import com.equibalance.dto.rebalance.DriftItem;
import com.equibalance.dto.rebalance.DriftResponse;
import com.equibalance.dto.rebalance.TradeDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RebalanceServiceTest {

    private final RebalanceService rebalanceService = new RebalanceService(null, null, null);

    @Test
    void generatesSellsBeforeBuys() {
        // AAPL is overweight (60% actual vs 50% target), BND underweight (40% vs 50%)
        DriftItem aapl = DriftItem.builder()
                .ticker("AAPL")
                .currentPrice(new BigDecimal("100"))
                .currentValue(new BigDecimal("600"))
                .currentPct(new BigDecimal("60"))
                .targetPct(new BigDecimal("50"))
                .driftPct(new BigDecimal("10"))
                .build();

        DriftItem bnd = DriftItem.builder()
                .ticker("BND")
                .currentPrice(new BigDecimal("50"))
                .currentValue(new BigDecimal("400"))
                .currentPct(new BigDecimal("40"))
                .targetPct(new BigDecimal("50"))
                .driftPct(new BigDecimal("-10"))
                .build();

        DriftResponse drift = DriftResponse.builder()
                .totalValue(new BigDecimal("1000"))
                .items(List.of(aapl, bnd))
                .build();

        List<TradeDto> trades = rebalanceService.buildTrades(drift);

        assertThat(trades).hasSize(2);
        assertThat(trades.get(0).getAction()).isEqualTo("SELL");
        assertThat(trades.get(0).getTicker()).isEqualTo("AAPL");
        assertThat(trades.get(1).getAction()).isEqualTo("BUY");
        assertThat(trades.get(1).getTicker()).isEqualTo("BND");

        // Selling 100 worth of AAPL at 100/share = 1 unit
        assertThat(trades.get(0).getUnits()).isEqualByComparingTo("1");
        // Buying 100 worth of BND at 50/share = 2 units
        assertThat(trades.get(1).getUnits()).isEqualByComparingTo("2");
    }

    @Test
    void noTradesWhenAlreadyBalanced() {
        DriftItem balanced = DriftItem.builder()
                .ticker("VTI")
                .currentPrice(new BigDecimal("100"))
                .currentValue(new BigDecimal("500"))
                .currentPct(new BigDecimal("50"))
                .targetPct(new BigDecimal("50"))
                .driftPct(BigDecimal.ZERO)
                .build();

        DriftResponse drift = DriftResponse.builder()
                .totalValue(new BigDecimal("1000"))
                .items(List.of(balanced))
                .build();

        assertThat(rebalanceService.buildTrades(drift)).isEmpty();
    }
}
