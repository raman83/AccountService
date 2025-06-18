package com.account.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.account.client.CustomerServiceClient;
import com.account.dto.AccountRequest;
import com.account.dto.AccountResponse;
import com.account.dto.CustomerResponse;
import com.account.model.Account;
import com.account.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository repo;
    private final CustomerServiceClient customerClient;

    public AccountResponse create(AccountRequest request) {
        ResponseEntity<CustomerResponse> customerResponse = customerClient.getCustomerByExternalId(request.getExternalCustomerId());

        Account acc = new Account();
        acc.setCustomerId(customerResponse.getBody().getId());
        acc.setAccountType(request.getAccountType());
        acc.setBalance(request.getInitialDeposit());
        acc.setCurrency(request.getCurrency());
        acc.setStatus("ACTIVE");
        acc.setCreatedAt(LocalDateTime.now());

        return toResponse(repo.save(acc));
    }

    public List<AccountResponse> findByCustomerId(String customerId) {
    	List<AccountResponse> accounts = repo.findByCustomerId(customerId).stream().map(this::toResponse).toList();
        return accounts;
    }

    private AccountResponse toResponse(Account acc) {
        return new AccountResponse(acc.getId(), acc.getCustomerId(), acc.getAccountType(),
                                   acc.getBalance(), acc.getCurrency(), acc.getStatus());
    }
}
