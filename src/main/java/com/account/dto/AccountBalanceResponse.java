package com.account.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountBalanceResponse {
    private UUID accountId;
    private BigDecimal availableBalance;
    private BigDecimal ledgerBalance;
    private String currency;
    private LocalDateTime balanceDateTime;
}