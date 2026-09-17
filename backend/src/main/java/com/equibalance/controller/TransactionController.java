package com.equibalance.controller;

import com.equibalance.dto.portfolio.TransactionResponse;
import com.equibalance.security.CurrentUserProvider;
import com.equibalance.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public List<TransactionResponse> list(@PathVariable Long portfolioId) {
        Long userId = currentUserProvider.getCurrentUserId();
        return transactionService.getTransactionLog(portfolioId, userId);
    }
}
