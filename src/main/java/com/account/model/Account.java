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
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String customerId;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    private AccountSubType accountSubType;

    private BigDecimal balance;
    private String currency;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    private String nickname;
    private String displayName;
    private BigDecimal interestRate;

    private String accountNumber;               // ✅ Stored but never exposed in full
    private String institutionId;               // ✅ Used for FDX compliance

    private LocalDateTime openDate;
    private LocalDateTime lastUpdatedDateTime;
}
