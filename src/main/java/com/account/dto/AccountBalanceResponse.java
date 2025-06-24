package com.account.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountBalanceResponse {
    private String accountId;
    private BigDecimal availableBalance;
    private BigDecimal ledgerBalance;
    private String currency;
    private LocalDateTime balanceDateTime;
}