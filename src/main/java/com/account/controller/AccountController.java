package com.account.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.account.dto.AccountRequest;
import com.account.service.AccountService;
import com.account.dto.AccountResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService service;

    @PostMapping("/accounts")
    public ResponseEntity<AccountResponse> create(@RequestBody AccountRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @GetMapping("/customers/{id}/accounts")
    public ResponseEntity<List<AccountResponse>> getByCustomer(@PathVariable("id") String id) {
        return ResponseEntity.ok(service.findByCustomerId(id));
    }
}
