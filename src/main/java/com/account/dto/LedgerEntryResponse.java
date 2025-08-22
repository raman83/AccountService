package com.account.dto;


import lombok.*;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntryResponse {
private UUID id;
private String side; // DEBIT/CREDIT
private BigDecimal amount;
private BigDecimal resultingLedgerBalance;
private LocalDateTime postedAt;
private String description;
private UUID externalTransactionId;
}