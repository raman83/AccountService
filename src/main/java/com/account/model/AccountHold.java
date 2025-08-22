package com.account.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "account_hold",
    indexes = {
        @Index(name = "idx_hold_account", columnList = "account_id"),
        @Index(name = "idx_hold_status", columnList = "status")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_hold_fp_per_account", columnNames = {"account_id", "requestFingerprint"})
    }
)
public class AccountHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    private String currency; // keep same as account currency

    @Enumerated(EnumType.STRING)
    private HoldStatus status; // ACTIVE, RELEASED, CANCELLED

    @Enumerated(EnumType.STRING)
    private HoldType type;     // CARD, CHEQUE, COMPLIANCE, OTHER

    @Column(length = 200)
    private String reason;

    /** Auto release at (created + 7 days by default). */
    private LocalDateTime releaseAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** For idempotency per account. */
    @Column(length = 64)
    private String requestFingerprint;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        createdAt = now;
        updatedAt = now;
        if (status == null) status = HoldStatus.ACTIVE;
        if (releaseAt == null) releaseAt = now.plusDays(7);
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now(ZoneId.of("UTC"));
    }
}