package com.account.dto;

import com.account.model.HoldType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CreateHoldRequest {

	private BigDecimal amount;
	private String currency; // default from account if null
	private String type; // CARD, CHEQUE, COMPLIANCE, OTHER
	private String reason;
	/** Optional override in days; default 7 */
	private Integer releaseAfterDays;
	

}
