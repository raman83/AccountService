package com.account.dto;

import com.account.model.HoldStatus;
import com.account.model.HoldType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class HoldResponse {

	private Long id;
	private BigDecimal amount;
	private String currency;
	private String status;
	private String type;
	private String reason;
	private LocalDateTime releaseAt;
	private LocalDateTime createdAt;
}
