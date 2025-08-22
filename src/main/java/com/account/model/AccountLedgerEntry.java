package com.account.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "account_ledger_entry",
       indexes = {
           @Index(name = "idx_ledger_account", columnList = "account_id"),
           @Index(name = "idx_ledger_posted", columnList = "postedAt")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_ledger_fp_per_account", columnNames = {"account_id","requestFingerprint"})
       })
public class AccountLedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Enumerated(EnumType.STRING)
    private LedgerSide side; // DEBIT or CREDIT

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(precision = 19, scale = 4)
    private BigDecimal resultingLedgerBalance;

    private LocalDateTime postedAt;

    @Column(length = 200)
    private String description;

    /** Optional link to the TransactionService id */
    private UUID externalTransactionId;

    /** For idempotency per account operation */
    @Column(length = 64)
    private String requestFingerprint;

    @PrePersist
    void prePersist() {
        if (postedAt == null) postedAt = LocalDateTime.now();
    }

    public enum LedgerSide { DEBIT, CREDIT }
}