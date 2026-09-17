package com.equibalance.service;

import com.equibalance.dto.rebalance.DriftItem;
import com.equibalance.dto.rebalance.DriftResponse;
import com.equibalance.dto.rebalance.RebalanceResponse;
import com.equibalance.dto.rebalance.TradeDto;
import com.equibalance.entity.Portfolio;
import com.equibalance.entity.RebalanceEvent;
import com.equibalance.repository.RebalanceEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

/**
 * Core rebalancing algorithm (PRD section 12) - O(n log n):
 *
 *  For each holding:
 *    targetValue(h) = targetPct(h) / 100 * totalValue
 *    delta(h)       = targetValue(h) - currentValue(h)   // +ve => BUY, -ve => SELL
 *
 *  Sort holdings: SELLs first (generates cash), then BUYs (uses cash generated).
 *  Trade unit count for each leg = |delta| / price, rounded down to keep the
 *  trade list conservative (never oversells cash on hand).
 */
@Service
@RequiredArgsConstructor
public class RebalanceService {

    private final DriftService driftService;
    private final PortfolioService portfolioService;
    private final RebalanceEventRepository rebalanceEventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public RebalanceResponse generateRebalance(Long portfolioId, Long userId) {
        Portfolio portfolio = portfolioService.findOwned(portfolioId, userId);
        DriftResponse drift = driftService.calculateDrift(portfolioId, userId);

        List<TradeDto> trades = buildTrades(drift);

        BigDecimal totalDriftBefore = drift.getItems().stream()
                .map(DriftItem::getDriftPct)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        RebalanceEvent event = RebalanceEvent.builder()
                .portfolio(portfolio)
                .totalDriftBefore(totalDriftBefore)
                .tradesJson(toJson(trades))
                .status("SUGGESTED")
                .build();
        rebalanceEventRepository.save(event);

        return RebalanceResponse.builder()
                .portfolioId(portfolioId)
                .triggeredAt(event.getTriggeredAt())
                .totalDriftBefore(totalDriftBefore)
                .trades(trades)
                .build();
    }

    List<TradeDto> buildTrades(DriftResponse drift) {
        BigDecimal totalValue = drift.getTotalValue();

        record Delta(String ticker, BigDecimal price, BigDecimal amount) {}

        List<Delta> deltas = drift.getItems().stream()
                .map(item -> {
                    BigDecimal targetValue = item.getTargetPct()
                            .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP)
                            .multiply(totalValue);
                    BigDecimal delta = targetValue.subtract(item.getCurrentValue());
                    return new Delta(item.getTicker(), item.getCurrentPrice(), delta);
                })
                .filter(d -> d.amount().abs().compareTo(BigDecimal.valueOf(0.01)) > 0)
                // SELLs (negative delta) first to free up cash, then BUYs
                .sorted(Comparator.comparing(d -> d.amount().signum()))
                .toList();

        return deltas.stream()
                .map(d -> {
                    boolean isBuy = d.amount().signum() > 0;
                    BigDecimal absAmount = d.amount().abs();
                    BigDecimal units = d.price().compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : absAmount.divide(d.price(), 6, RoundingMode.DOWN);

                    return TradeDto.builder()
                            .ticker(d.ticker())
                            .action(isBuy ? "BUY" : "SELL")
                            .units(units)
                            .estimatedPrice(d.price())
                            .estimatedCost(absAmount)
                            .build();
                })
                .toList();
    }

    @SneakyThrows
    private String toJson(List<TradeDto> trades) {
        return objectMapper.writeValueAsString(trades);
    }
}
