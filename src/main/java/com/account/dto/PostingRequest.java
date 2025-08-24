package com.account.dto;


import lombok.*;


import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostingRequest {
private BigDecimal amount; // positive amount
private String currency; // optional; default from account
private String description; // e.g. “Transfer to xxxx”
private String externalRefId; // optional, e.g. tx id from Payment/RTR/ACH


//FIELDS (for auto-hold on external credits)
private String source;          // "INTERNAL" or "EXTERNAL"
private Boolean createHold;     // optional override; if null, derived from source
private Integer holdDays;       // optional override; default 7 (if provided)
private String holdType;        // CARD, CHEQUE, COMPLIANCE, OTHER (optional)
private String holdReason;      // e.g., "External credit hold"



}