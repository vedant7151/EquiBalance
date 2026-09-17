package com.equibalance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "rebalance_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RebalanceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(name = "triggered_at", nullable = false, updatable = false)
    private Instant triggeredAt;

    @Column(name = "total_drift_before", nullable = false, precision = 8, scale = 4)
    private BigDecimal totalDriftBefore;

    /** JSON-serialized list of TradeDto suggested at the time of this event. */
    @Lob
    @Column(name = "trades_json", nullable = false, columnDefinition = "TEXT")
    private String tradesJson;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "SUGGESTED"; // SUGGESTED | APPLIED

    @PrePersist
    protected void onCreate() {
        this.triggeredAt = Instant.now();
    }
}
