package com.account.service;

import com.account.client.CustomerServiceClient;
import com.account.dto.AccountBalanceResponse;
import com.account.dto.AccountRequest;
import com.account.dto.AccountResponse;
import com.account.exception.AccountNotFoundException;
import com.account.exception.CustomerNotFoundException;
import com.account.exception.InsufficientFundsException;
import com.account.mapper.AccountMapper;
import com.account.model.Account;
import com.account.model.AccountStatus;
import com.account.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repo;
    private final CustomerServiceClient customerClient;
    private final AccountMapper mapper;

    public AccountResponse create(AccountRequest request) {
        log.info(" Validating customer existence: {}", request.getCustomerId());
        if (!customerClient.exists(request.getCustomerId())) {
            throw new  CustomerNotFoundException(request.getCustomerId());
        }

        Account account = mapper.toEntity(request);

        if (account.getOpenDate() == null) {
            account.setOpenDate(LocalDateTime.now());
        }

        if (account.getAccountNumber() == null) {
            account.setAccountNumber(generateAccountNumber());
        }

        account.setInstitutionId("CANADA001"); // Static for now; dynamic later
        account.setLastUpdatedDateTime(LocalDateTime.now());

        Account saved = repo.save(account);
        log.info("Account created: id={}, customerId={}", saved.getId(), saved.getCustomerId());
        return mapper.toDto(saved);
    }
    
    
    @Transactional
    public void updateStatus(UUID accountId, AccountStatus newStatus) {
        Account acc = repo.findById(accountId)
                          .orElseThrow(() -> new AccountNotFoundException(accountId));
        acc.setStatus(newStatus);
        repo.save(acc);
    }
    
    @Transactional
    public void creditAccount(UUID accountId, BigDecimal amount) {
        Account acc = repo.findById(accountId)
                          .orElseThrow(() -> new AccountNotFoundException(accountId));
        acc.setBalance(acc.getBalance().add(amount));
        repo.save(acc);
    }
    
    public AccountBalanceResponse getBalanceInfo(UUID id) {
        Account acc = repo.findById(id)
                          .orElseThrow(() -> new AccountNotFoundException(id));
        return AccountBalanceResponse.builder()
                .accountId(acc.getId())
                .currency(acc.getCurrency())
                .availableBalance(acc.getBalance())
                .ledgerBalance(acc.getBalance()) // update if ledger differs
                .balanceDateTime(LocalDateTime.now())
                .build();
    }
 
    public List<AccountResponse> getAllAccounts() {
        return repo.findAll().stream().map(mapper::toDto).toList();
    }
    
    public AccountResponse findById(UUID id) {
        return repo.findById(id)
                   .map(mapper::toDto)
                   .orElseThrow(() -> new AccountNotFoundException(id));
    }

    public List<AccountResponse> findByCustomerId(String customerId) {
        log.info("📄 Listing accounts for customerId={}", customerId);
        return repo.findByCustomerId(customerId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void debitAccount(UUID accountId, BigDecimal amount) {
        Account account = repo.findById(accountId)
                .orElseThrow(() -> {
                    log.error(" Account not found: {}", accountId);
                    return new AccountNotFoundException(accountId);
                });

        if (account.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient funds: accountId={}, balance={}, requested={}",
                     accountId, account.getBalance(), amount);
            throw new InsufficientFundsException();
        }

        BigDecimal oldBalance = account.getBalance();
        account.setBalance(oldBalance.subtract(amount));
        account.setLastUpdatedDateTime(LocalDateTime.now());
        repo.save(account);

        log.info(" Debit successful: accountId={}, oldBalance={}, newBalance={}",
                 accountId, oldBalance, account.getBalance());
    }
    
    
    public List<AccountResponse> searchAccounts(String status, String currency) {
        List<Account> all = repo.findAll();
        return all.stream()
                .filter(a -> status == null || a.getStatus().name().equalsIgnoreCase(status))
                .filter(a -> currency == null || a.getCurrency().equalsIgnoreCase(currency))
                .map(mapper::toDto)
                .toList();
    }

    private String generateAccountNumber() {
        return "CAN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
    
    
    public String getCustomerIdForAccount(UUID accountId) {
        Account account = repo.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(accountId));
        return account.getCustomerId();
    }
}
