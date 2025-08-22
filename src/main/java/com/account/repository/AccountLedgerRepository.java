package com.account.repository;


import com.account.model.AccountLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;


public interface AccountLedgerRepository extends JpaRepository<AccountLedgerEntry, UUID> {
Optional<AccountLedgerEntry> findByAccountIdAndRequestFingerprint(UUID accountId, String requestFingerprint);
}