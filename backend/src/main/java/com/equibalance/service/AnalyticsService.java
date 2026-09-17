package com.equibalance.service;

import com.equibalance.dto.analytics.AllocationSlice;
import com.equibalance.dto.analytics.AnalyticsResponse;
import com.equibalance.dto.analytics.RebalanceHistoryItem;
import com.equibalance.dto.analytics.ValuePoint;
import com.equibalance.dto.portfolio.HoldingResponse;
import com.equibalance.dto.portfolio.PortfolioResponse;
import com.equibalance.entity.RebalanceEvent;
import com.equibalance.entity.Transaction;
import com.equibalance.repository.RebalanceEventRepository;
import com.equibalance.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Analytics is built from data already on hand: live holding valuations, the
 * transaction log, and rebalance events. There is no separate time-series price
 * store yet, so the "performance history" line is reconstructed from cumulative
 * transaction cash flow rather than true daily mark-to-market - documented as a
 * known simplification in the README.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PortfolioService portfolioService;
    private final TransactionRepository transactionRepository;
    private final RebalanceEventRepository rebalanceEventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalyticsResponse getAnalytics(Long portfolioId, Long userId) {
        PortfolioResponse portfolio = portfolioService.getForUser(portfolioId, userId);

        List<AllocationSlice> current = new ArrayList<>();
        List<AllocationSlice> target = new ArrayList<>();
        for (HoldingResponse h : portfolio.getHoldings()) {
            current.add(AllocationSlice.builder()
                    .ticker(h.getTicker())
                    .percentage(h.getCurrentAllocationPct())
                    .value(h.getCurrentValue())
                    .build());
            target.add(AllocationSlice.builder()
                    .ticker(h.getTicker())
                    .percentage(h.getTargetAllocationPct())
                    .value(portfolio.getTotalValue() == null ? BigDecimal.ZERO :
                            h.getTargetAllocationPct().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                                    .multiply(portfolio.getTotalValue()))
                    .build());
        }

        List<Transaction> transactions =
                transactionRepository.findAllByPortfolioIdOrderByExecutedAtDesc(portfolioId);

        List<ValuePoint> performanceHistory = buildPerformanceHistory(transactions);

        List<RebalanceEvent> events = rebalanceEventRepository.findAllByPortfolioIdOrderByTriggeredAtDesc(portfolioId);
        List<RebalanceHistoryItem> rebalanceHistory = events.stream()
                .map(e -> RebalanceHistoryItem.builder()
                        .id(e.getId())
                        .triggeredAt(e.getTriggeredAt())
                        .totalDriftBefore(e.getTotalDriftBefore())
                        .status(e.getStatus())
                        .tradesCount(countTrades(e.getTradesJson()))
                        .build())
                .toList();

        return AnalyticsResponse.builder()
                .totalValue(portfolio.getTotalValue())
                .dayGainLoss(BigDecimal.ZERO) // requires previous-close snapshot - not yet tracked
                .dayGainLossPct(BigDecimal.ZERO)
                .overallReturnPct(BigDecimal.ZERO) // requires cost-basis tracking - not yet implemented
                .currentAllocation(current)
                .targetAllocation(target)
                .performanceHistory(performanceHistory)
                .rebalanceHistory(rebalanceHistory)
                .build();
    }

    private List<ValuePoint> buildPerformanceHistory(List<Transaction> transactionsDesc) {
        List<Transaction> chronological = new ArrayList<>(transactionsDesc);
        java.util.Collections.reverse(chronological);

        List<ValuePoint> points = new ArrayList<>();
        BigDecimal runningValue = BigDecimal.ZERO;
        for (Transaction t : chronological) {
            BigDecimal delta = t.getUnits().multiply(t.getPriceAtTime());
            boolean increases = t.getType().name().equals("BUY") || t.getType().name().equals("ADD");
            runningValue = increases ? runningValue.add(delta) : runningValue.subtract(delta);
            points.add(ValuePoint.builder()
                    .timestamp(t.getExecutedAt())
                    .value(runningValue)
                    .build());
        }
        return points;
    }

    @SneakyThrows
    private int countTrades(String tradesJson) {
        return objectMapper.readTree(tradesJson).size();
    }
}
