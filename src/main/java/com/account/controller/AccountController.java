package com.account.controller;

import com.account.dto.AccountBalanceResponse;
import com.account.dto.AccountRequest;
import com.account.dto.AccountResponse;
import com.account.model.AccountStatus;
import com.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService service;

    @PostMapping("/accounts")
    @PreAuthorize("hasAuthority('SCOPE_fdx:accounts.write')")
    public ResponseEntity<AccountResponse> create(@RequestBody AccountRequest request) {
        log.info("Creating account for customer: {}", request.getCustomerId());
        return ResponseEntity.ok(service.create(request));
    }
    
    @GetMapping("/accounts/{id}")
    @PreAuthorize("hasAuthority('fdx:accounts.read')")

    public ResponseEntity<AccountResponse> getById(@PathVariable("id") String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/customers/{id}/accounts")
    @PreAuthorize("hasAuthority('fdx:accounts.read')")

    public ResponseEntity<List<AccountResponse>> getByCustomer(@PathVariable("id") String id) {
        log.info("Fetching accounts for customer: {}", id);
        return ResponseEntity.ok(service.findByCustomerId(id));
    }
    

    @GetMapping("/accounts")
    @PreAuthorize("hasAuthority('fdx:accounts.read')")

    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(service.getAllAccounts());
    }
    
    
    @GetMapping("/accounts/{id}/balances")
    @PreAuthorize("hasAuthority('fdx:accounts.read')")

    public ResponseEntity<AccountBalanceResponse> getBalances(@PathVariable("id") String id) {
        return ResponseEntity.ok(service.getBalanceInfo(id));
    }
    
    
    @PostMapping("/accounts/{id}/credit")
    @PreAuthorize("hasAuthority('fdx:accounts.write')")
    public ResponseEntity<Void> creditAccount(
            @PathVariable("id") String id,
            @RequestParam("amount") BigDecimal amount) {
        service.creditAccount(id, amount);
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/accounts/{id}/status")
    @PreAuthorize("hasAuthority('fdx:accounts.write')")
    public ResponseEntity<Void> updateStatus(
            @PathVariable("id") String id,
            @RequestParam("status") AccountStatus status) {
        service.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/accounts/{id}/debit")
    @PreAuthorize("hasAuthority('fdx:accounts.write')")
    public ResponseEntity<Void> debitAccount(@PathVariable("id") String id,
                                             @RequestParam("amount") BigDecimal amount) {
        log.info(" Debit request: accountId={}, amount={}", id, amount);
        service.debitAccount(id, amount);
        return ResponseEntity.ok().build();
    }
    
    
    @GetMapping("/accounts/search")
    @PreAuthorize("hasAuthority('fdx:accounts.read')")
    public ResponseEntity<List<AccountResponse>> searchAccounts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String currency) {
        return ResponseEntity.ok(service.searchAccounts(status, currency));
    }
}
