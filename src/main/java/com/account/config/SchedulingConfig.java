package com.account.config;

import com.account.model.HoldStatus;
import com.account.repository.AccountHoldRepository;
import com.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig {

    private final AccountHoldRepository holdRepo;
    private final AccountRepository accountRepo;

    // Run every minute; release expired holds
    @Scheduled(fixedDelay = 60_000)
    public void autoReleaseHolds() {
        var now = LocalDateTime.now();
        holdRepo.findByStatusAndReleaseAtLessThanEqual(HoldStatus.ACTIVE, now).forEach(h -> {
            h.setStatus(HoldStatus.RELEASED);
            holdRepo.save(h);
            var acc = h.getAccount();
            var holds = holdRepo.sumByAccountAndStatus(acc.getId(), HoldStatus.ACTIVE);
            acc.setAvailableBalance(acc.getLedgerBalance().subtract(holds == null ? BigDecimal.ZERO : holds));
            accountRepo.save(acc);
        });
    }
}