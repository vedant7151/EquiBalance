package com.equibalance.repository;

import com.equibalance.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByPortfolioIdOrderByExecutedAtDesc(Long portfolioId);
}
