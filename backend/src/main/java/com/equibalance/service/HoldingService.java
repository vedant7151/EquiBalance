package com.equibalance.service;

import com.equibalance.dto.PriceResponse;
import com.equibalance.dto.portfolio.HoldingRequest;
import com.equibalance.entity.Holding;
import com.equibalance.entity.Portfolio;
import com.equibalance.entity.Transaction;
import com.equibalance.entity.TransactionType;
import com.equibalance.exception.ResourceNotFoundException;
import com.equibalance.repository.HoldingRepository;
import com.equibalance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final TransactionRepository transactionRepository;
    private final PortfolioService portfolioService;
    private final PriceService priceService;

    @Transactional
    public void addHolding(Long portfolioId, Long userId, HoldingRequest request) {
        Portfolio portfolio = portfolioService.findOwned(portfolioId, userId);

        Holding holding = Holding.builder()
                .portfolio(portfolio)
                .ticker(request.getTicker().toUpperCase())
                .units(request.getUnits())
                .targetAllocationPct(request.getTargetAllocationPct())
                .build();

        holding = holdingRepository.save(holding);
        logTransaction(portfolio, holding, TransactionType.ADD, request.getUnits());
    }

    @Transactional
    public void updateHolding(Long portfolioId, Long holdingId, Long userId, HoldingRequest request) {
        portfolioService.findOwned(portfolioId, userId); // ownership check
        Holding holding = holdingRepository.findByIdAndPortfolioId(holdingId, portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Holding " + holdingId + " not found"));

        BigDecimal previousUnits = holding.getUnits();
        holding.setUnits(request.getUnits());
        holding.setTargetAllocationPct(request.getTargetAllocationPct());
        holding = holdingRepository.save(holding);

        BigDecimal delta = request.getUnits().subtract(previousUnits);
        if (delta.compareTo(BigDecimal.ZERO) != 0) {
            TransactionType type = delta.compareTo(BigDecimal.ZERO) > 0 ? TransactionType.BUY : TransactionType.SELL;
            logTransaction(holding.getPortfolio(), holding, type, delta.abs());
        }
    }

    @Transactional
    public void removeHolding(Long portfolioId, Long holdingId, Long userId) {
        portfolioService.findOwned(portfolioId, userId);
        Holding holding = holdingRepository.findByIdAndPortfolioId(holdingId, portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Holding " + holdingId + " not found"));

        logTransaction(holding.getPortfolio(), holding, TransactionType.REMOVE, holding.getUnits());
        holdingRepository.delete(holding);
    }

    private void logTransaction(Portfolio portfolio, Holding holding, TransactionType type, BigDecimal units) {
        BigDecimal price = BigDecimal.ZERO;
        try {
            PriceResponse priceResponse = priceService.getPrice(holding.getTicker());
            price = priceResponse.getPrice();
        } catch (Exception ignored) {
            // price unavailable - log with 0, still preserves the audit trail per PRD 6.5
        }

        Transaction transaction = Transaction.builder()
                .portfolio(portfolio)
                .holding(holding)
                .ticker(holding.getTicker())
                .type(type)
                .units(units)
                .priceAtTime(price)
                .build();

        transactionRepository.save(transaction);
    }
}
