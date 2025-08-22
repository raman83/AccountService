package com.account.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;

import com.account.model.Account;

public interface AccountRepository extends JpaRepository<Account, String> {
	List<Account> findByCustomerId(String customerId);
	Optional<Account> findByRequestFingerprint(String fingerprint);
	Optional<Account> findById(UUID id);


}