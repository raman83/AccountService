package com.account.dto;


import java.math.BigDecimal;

import lombok.Data;

@Data
public class AccountRequest{
	
	    String externalCustomerId; // will call CustomerService if present
	    String accountType;
	    BigDecimal initialDeposit;
	    String currency;

	    }
