package com.account.controller;

import com.account.dto.AccountBalanceResponse;
import com.account.dto.AccountOwnerResponse;
import com.account.dto.AccountRequest;
import com.account.dto.AccountResponse;
import com.account.dto.CreateHoldRequest;
import com.account.dto.HoldResponse;
import com.account.dto.LedgerEntryResponse;
import com.account.dto.PostingRequest;
import com.account.model.AccountStatus;
import com.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService service;

    @PostMapping("/accounts")
    @PreAuthorize("hasAuthority('SCOPE_fdx:accounts.write')")
    public ResponseEntity<AccountResponse> create(
    		@RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    		@RequestBody AccountRequest request) {
    		AccountResponse resp = service.create(request, idempotencyKey);
    		
    		return ResponseEntity.status(HttpStatus.CREATED)
    		.eTag('"' + String.valueOf(resp.getVersion()) + '"')
    		.body(resp);
    		}
    
    
    @GetMapping("/accounts/{id}")
    @PreAuthorize("hasAuthority('SCOPE_fdx:accounts.read')")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable("id") UUID id) {
    	AccountResponse r = service.findById(id);
    	return ResponseEntity.ok().eTag('"' + String.valueOf(r.getVersion()) + '"').body(r);
    	}
    
    
    
    @GetMapping("/accounts/search")
    @PreAuthorize("hasAuthority('SCOPE_fdx:accounts.read')")
    public ResponseEntity<List<AccountResponse>> searchAccounts(
    		@RequestParam(name = "status",required = false) AccountStatus status,
    		@RequestParam(name = "currency",required = false) String currency) {
    		// you may already have different search signature; keep existing behavior
    		return ResponseEntity.ok(service.getAllAccounts());
    		}
    
    
    
    @GetMapping("/accounts/{id}/balance")
    @PreAuthorize("hasAuthority('SCOPE_fdx:accounts.read')")

    public ResponseEntity<AccountBalanceResponse> getBalances(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(service.getBalance(id));
    }
    
 
    @GetMapping("/customer/{id}/accounts")
    @PreAuthorize("hasAuthority('SCOPE_fdx:accounts.read')")
    public ResponseEntity<List<AccountResponse>> getByCustomer(@PathVariable("id") String id) {
        log.info("Fetching accounts for customer: {}", id);
        return ResponseEntity.ok(service.findByCustomerId(id));
    }
    
    
    @PatchMapping("/accounts/{id}/status")
  //  @PreAuthorize("hasAuthority('SCOPE_fdx:accounts.write')")
    public ResponseEntity<Void> updateStatus(
            @PathVariable("id") UUID id,
            @RequestParam("status") AccountStatus status) {
        service.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }
    
    
    @GetMapping("/accounts/{id}/owner")
    //  @PreAuthorize("hasAuthority('SCOPE_fdx:accounts.read')")
      public AccountOwnerResponse getAccountOwner(@PathVariable("id") UUID id) {
          String customerId = service.getCustomerIdForAccount(id);
          return new AccountOwnerResponse(id, customerId);
      }
    
    
    
    @PostMapping("/accounts/{id}/holds")
    public ResponseEntity<HoldResponse> placeHold(
    @PathVariable("id") UUID id,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody CreateHoldRequest request) {
    HoldResponse resp = service.placeHold(id, request, idempotencyKey);
    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }
    

    @PostMapping("/accounts/{id}/holds/{holdId}/release")
    public ResponseEntity<Void> releaseHold(
    @PathVariable("id") UUID id,
    @PathVariable("holdId") Long holdId,
    @RequestHeader(name = "If-Match", required = false) String ifMatch) {
    Integer expected = parseIfMatch(ifMatch);
    service.releaseHold(id, holdId, expected);
    return ResponseEntity.noContent().build();
    }
    
    
    
    
    @PostMapping("/accounts/{id}/credit")
    @PreAuthorize("hasAuthority('SCOPE_fdx:accounts.write')")
    public ResponseEntity<LedgerEntryResponse> credit(
    		@PathVariable("id") UUID id,
    		@RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    		@RequestHeader(name = "If-Match", required = false) String ifMatch,
    		@RequestBody PostingRequest request) {
    		Integer expected = parseIfMatch(ifMatch);
    		LedgerEntryResponse r = service.credit(id, request, idempotencyKey, expected);
    		return ResponseEntity.status(HttpStatus.CREATED).eTag(ifMatch).body(r);
    		}


    @PostMapping("/accounts/{id}/debit")
    @PreAuthorize("hasAuthority('SCOPE_fdx:accounts.write')")
    public ResponseEntity<LedgerEntryResponse> debit(
    		@PathVariable("id") UUID id,
    		@RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    		@RequestHeader(name = "If-Match", required = false) String ifMatch,
    		@RequestBody PostingRequest request) {
    		Integer expected = parseIfMatch(ifMatch);
    		LedgerEntryResponse r = service.debit(id, request, idempotencyKey, expected);
    		return ResponseEntity.status(HttpStatus.CREATED).eTag(ifMatch).body(r);
    		}
    
   

    private Integer parseIfMatch(String ifMatch) {
    	if (ifMatch == null || ifMatch.isBlank()) return null;
    	String v = ifMatch.replace("\"", "").trim();
    	return Integer.valueOf(v);
    	}
  
}

