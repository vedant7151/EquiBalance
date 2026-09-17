package com.equibalance.service;

import com.equibalance.dto.portfolio.TransactionResponse;
import com.equibalance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final PortfolioService portfolioService;

    public List<TransactionResponse> getTransactionLog(Long portfolioId, Long userId) {
        portfolioService.findOwned(portfolioId, userId); // ownership check
        return transactionRepository.findAllByPortfolioIdOrderByExecutedAtDesc(portfolioId).stream()
                .map(t -> TransactionResponse.builder()
                        .id(t.getId())
                        .ticker(t.getTicker())
                        .type(t.getType().name())
                        .units(t.getUnits())
                        .priceAtTime(t.getPriceAtTime())
                        .executedAt(t.getExecutedAt())
                        .build())
                .toList();
    }
}
