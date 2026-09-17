package com.equibalance.service;

import com.equibalance.dto.PriceResponse;
import com.equibalance.dto.rebalance.DriftItem;
import com.equibalance.dto.rebalance.DriftResponse;
import com.equibalance.entity.Holding;
import com.equibalance.entity.Portfolio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calculates per-holding drift from target allocation (PRD section 12).
 *
 * currentValue(h) = units * price
 * currentPct(h)   = currentValue(h) / totalValue * 100
 * drift(h)        = currentPct(h) - targetPct(h)
 */
@Service
@RequiredArgsConstructor
public class DriftService {

    private final PortfolioService portfolioService;
    private final PriceService priceService;

    public DriftResponse calculateDrift(Long portfolioId, Long userId) {
        Portfolio portfolio = portfolioService.findOwned(portfolioId, userId);
        List<Holding> holdings = portfolio.getHoldings();

        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal[] currentValues = new BigDecimal[holdings.size()];
        BigDecimal[] prices = new BigDecimal[holdings.size()];

        for (int i = 0; i < holdings.size(); i++) {
            Holding h = holdings.get(i);
            BigDecimal price = fetchPriceOrZero(h.getTicker());
            prices[i] = price;
            BigDecimal value = price.multiply(h.getUnits());
            currentValues[i] = value;
            totalValue = totalValue.add(value);
        }

        List<DriftItem> items = new java.util.ArrayList<>();
        boolean needsRebalancing = false;

        for (int i = 0; i < holdings.size(); i++) {
            Holding h = holdings.get(i);
            BigDecimal currentPct = totalValue.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : currentValues[i].multiply(BigDecimal.valueOf(100)).divide(totalValue, 4, RoundingMode.HALF_UP);
            BigDecimal drift = currentPct.subtract(h.getTargetAllocationPct());

            if (drift.abs().compareTo(portfolio.getRebalanceThresholdPct()) > 0) {
                needsRebalancing = true;
            }

            items.add(DriftItem.builder()
                    .ticker(h.getTicker())
                    .units(h.getUnits())
                    .currentPrice(prices[i])
                    .currentValue(currentValues[i])
                    .currentPct(currentPct)
                    .targetPct(h.getTargetAllocationPct())
                    .driftPct(drift)
                    .build());
        }

        return DriftResponse.builder()
                .portfolioId(portfolio.getId())
                .totalValue(totalValue)
                .needsRebalancing(needsRebalancing)
                .rebalanceThresholdPct(portfolio.getRebalanceThresholdPct())
                .items(items)
                .build();
    }

    private BigDecimal fetchPriceOrZero(String ticker) {
        try {
            PriceResponse response = priceService.getPrice(ticker);
            return response.getPrice();
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
