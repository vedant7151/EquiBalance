package com.equibalance.service;

import com.equibalance.dto.PriceResponse;
import com.equibalance.dto.portfolio.HoldingResponse;
import com.equibalance.dto.portfolio.PortfolioRequest;
import com.equibalance.dto.portfolio.PortfolioResponse;
import com.equibalance.entity.Portfolio;
import com.equibalance.entity.User;
import com.equibalance.exception.ResourceNotFoundException;
import com.equibalance.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PriceService priceService;

    @Transactional
    public PortfolioResponse create(User user, PortfolioRequest request) {
        Portfolio portfolio = Portfolio.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .baseCurrency(request.getBaseCurrency() == null ? "USD" : request.getBaseCurrency())
                .rebalanceThresholdPct(request.getRebalanceThresholdPct())
                .build();

        portfolio = portfolioRepository.save(portfolio);
        return toResponse(portfolio);
    }

    public List<PortfolioResponse> listForUser(Long userId) {
        return portfolioRepository.findAllByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public PortfolioResponse getForUser(Long id, Long userId) {
        Portfolio portfolio = findOwned(id, userId);
        return toResponse(portfolio);
    }

    @Transactional
    public PortfolioResponse update(Long id, Long userId, PortfolioRequest request) {
        Portfolio portfolio = findOwned(id, userId);
        portfolio.setName(request.getName());
        portfolio.setDescription(request.getDescription());
        if (request.getBaseCurrency() != null) {
            portfolio.setBaseCurrency(request.getBaseCurrency());
        }
        if (request.getRebalanceThresholdPct() != null) {
            portfolio.setRebalanceThresholdPct(request.getRebalanceThresholdPct());
        }
        return toResponse(portfolioRepository.save(portfolio));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Portfolio portfolio = findOwned(id, userId);
        portfolioRepository.delete(portfolio);
    }

    protected Portfolio findOwned(Long id, Long userId) {
        return portfolioRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio " + id + " not found"));
    }

    /** Builds the response including live valuation - used by list/detail endpoints. */
    PortfolioResponse toResponse(Portfolio portfolio) {
        List<HoldingResponse> holdingResponses = portfolio.getHoldings().stream()
                .map(h -> {
                    PriceResponse price = safePrice(h.getTicker());
                    BigDecimal currentValue = price == null
                            ? BigDecimal.ZERO
                            : price.getPrice().multiply(h.getUnits());
                    return HoldingResponse.builder()
                            .id(h.getId())
                            .ticker(h.getTicker())
                            .units(h.getUnits())
                            .targetAllocationPct(h.getTargetAllocationPct())
                            .currentPrice(price == null ? null : price.getPrice())
                            .stale(price == null || price.isStale())
                            .currentValue(currentValue)
                            .addedAt(h.getAddedAt())
                            .build();
                })
                .toList();

        BigDecimal totalValue = holdingResponses.stream()
                .map(HoldingResponse::getCurrentValue)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean needsRebalancing = false;
        for (HoldingResponse hr : holdingResponses) {
            BigDecimal currentPct = totalValue.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : hr.getCurrentValue().multiply(BigDecimal.valueOf(100))
                        .divide(totalValue, 4, RoundingMode.HALF_UP);
            hr.setCurrentAllocationPct(currentPct);
            BigDecimal drift = currentPct.subtract(hr.getTargetAllocationPct()).abs();
            hr.setDriftPct(drift);
            if (drift.compareTo(portfolio.getRebalanceThresholdPct()) > 0) {
                needsRebalancing = true;
            }
        }

        return PortfolioResponse.builder()
                .id(portfolio.getId())
                .name(portfolio.getName())
                .description(portfolio.getDescription())
                .baseCurrency(portfolio.getBaseCurrency())
                .rebalanceThresholdPct(portfolio.getRebalanceThresholdPct())
                .createdAt(portfolio.getCreatedAt())
                .totalValue(totalValue)
                .needsRebalancing(needsRebalancing)
                .holdings(holdingResponses)
                .build();
    }

    private PriceResponse safePrice(String ticker) {
        try {
            return priceService.getPrice(ticker);
        } catch (Exception e) {
            return null;
        }
    }
}
