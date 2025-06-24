package com.account.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.account.model.AccountStatus;
import com.account.model.AccountSubType;
import com.account.model.AccountType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountRequest {
    private String customerId;
    private AccountType accountType;
    private AccountSubType accountSubType;
    private BigDecimal balance;
    private String currency;
    private AccountStatus status;

    private String nickname;
    private String displayName;
    private BigDecimal interestRate;

    private String institutionId;

    private LocalDateTime openDate; // Optional: usually set to now
}
