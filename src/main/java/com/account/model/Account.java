package com.account.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "account",
    indexes = {
        @Index(name = "idx_account_customer", columnList = "customerId")
    },
    uniqueConstraints = {
        // For create‑idempotency if you already compute a requestFingerprint on create
        @UniqueConstraint(name = "uk_account_request_fp", columnNames = {"requestFingerprint"})
    }
)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String customerId;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    private AccountSubType accountSubType;

    /** Total book balance. */
    @Column(precision = 19, scale = 4)
    private BigDecimal ledgerBalance;

    /** Available = ledger − active holds. We maintain it for fast reads, but always recompute on write. */
    @Column(precision = 19, scale = 4)
    private BigDecimal availableBalance;

    private String currency; // e.g., "CAD"

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    private String nickname;
    private String displayName;

    @Column(precision = 9, scale = 4)
    private BigDecimal interestRate;

    /** Stored full account number (never exposed fully). */
    private String accountNumber;

    /** FDX field you asked to keep. */
    private String institutionId;

    private LocalDateTime openDate;
    private LocalDateTime lastUpdatedDateTime;

    /** For create‑idempotency (optional). */
    @Column(length = 64)
    private String requestFingerprint;

    /** JPA optimistic locking. Surfaces as ETag. */
    @Version
    private Integer version;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        if (openDate == null) openDate = now;
        lastUpdatedDateTime = now;
        if (ledgerBalance == null) ledgerBalance = new BigDecimal("0.00");
        if (availableBalance == null) availableBalance = ledgerBalance;
        if (status == null) status = AccountStatus.ACTIVE;
        if (currency == null) currency = "CAD";
        if (version == null) version = 0;
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdatedDateTime = LocalDateTime.now(ZoneId.of("UTC"));
    }
}