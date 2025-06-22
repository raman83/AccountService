package com.account.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.account.model.Account;

public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findByCustomerId(String customerId);
    Optional<Account> findById(String Id);

}