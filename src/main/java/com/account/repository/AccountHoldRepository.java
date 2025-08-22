package com.account.repository;

import com.account.model.AccountHold;
import com.account.model.HoldStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountHoldRepository extends JpaRepository<AccountHold, Long> {

	@Query("select h from AccountHold h where h.account.id = ?1 and h.requestFingerprint = ?2")
	Optional<AccountHold> findByAccountAndFingerprint(UUID accountId, String requestFingerprint);


	@Query("select coalesce(sum(h.amount), 0) from AccountHold h where h.account.id = ?1 and h.status = ?2")
	BigDecimal sumByAccountAndStatus(UUID accountId, HoldStatus status);


	List<AccountHold> findByStatusAndReleaseAtLessThanEqual(HoldStatus status, LocalDateTime now);}
