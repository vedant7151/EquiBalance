package com.equibalance.repository;

import com.equibalance.entity.RebalanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RebalanceEventRepository extends JpaRepository<RebalanceEvent, Long> {
    List<RebalanceEvent> findAllByPortfolioIdOrderByTriggeredAtDesc(Long portfolioId);
}
