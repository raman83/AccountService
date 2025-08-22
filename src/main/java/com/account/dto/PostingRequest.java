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
}