package com.account.service;

import com.account.client.CustomerServiceClient;
import com.account.dto.AccountBalanceResponse;
import com.account.dto.AccountRequest;
import com.account.dto.AccountResponse;
import com.account.dto.CreateHoldRequest;
import com.account.dto.HoldResponse;
import com.account.dto.LedgerEntryResponse;
import com.account.dto.PostingRequest;
import com.account.mapper.AccountMapper;
import com.account.model.Account;
import com.account.model.AccountHold;
import com.account.model.AccountLedgerEntry;
import com.account.model.AccountStatus;
import com.account.model.HoldStatus;
import com.account.model.HoldType;
import com.account.repository.AccountHoldRepository;
import com.account.repository.AccountLedgerRepository;
import com.account.repository.AccountRepository;
import com.account.util.Fingerprints;
import com.commons.exception.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repo;
    private final CustomerServiceClient customerClient;
    private final AccountHoldRepository holdRepo;
    private final AccountLedgerRepository ledgerRepo;
    private final AccountMapper mapper;
    
    
    public List<AccountResponse> getAllAccounts() {
        return repo.findAll().stream().map(mapper::toDto).toList();
    }
    
    public AccountResponse findById(UUID id) {
    	 Account entity = repo.findById(id)
    		        .orElseThrow(() -> new AccountNotFoundException(id));
    	    return mapper.toDto(entity);

    	 
    }
    
    
    public List<AccountResponse> findByCustomerId(String customerId) {
        log.info("📄 Listing accounts for customerId={}", customerId);
        return repo.findByCustomerId(customerId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }
    
    
    public String getCustomerIdForAccount(UUID accountId) {
        Account account = repo.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(accountId));
        return account.getCustomerId();
    }
    
    public AccountBalanceResponse getBalance(UUID accountId) {
    	Account acc = repo.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
    	BigDecimal holds = holdRepo.sumByAccountAndStatus(accountId, HoldStatus.ACTIVE);
    	BigDecimal available = acc.getLedgerBalance().subtract(holds == null ? BigDecimal.ZERO : holds);
    	acc.setAvailableBalance(available);
    	return AccountBalanceResponse.builder()
    	.id(acc.getId())
    	.ledgerBalance(acc.getLedgerBalance())
    	.availableBalance(available)
    	.currency(acc.getCurrency())
    	.balanceDateTime(LocalDateTime.now())
    	.build();
    	}
    
    
    @Transactional
    public AccountResponse create(AccountRequest request,String idempotencyKey) {
        log.info(" Validating customer existence: {}", request.getCustomerId());
        if (!customerClient.exists(request.getCustomerId())) {
            throw new  CustomerNotFoundException(request.getCustomerId());
        }
        String fp = (idempotencyKey != null && !idempotencyKey.isBlank())
        		? idempotencyKey.trim()
        		: Fingerprints.createAccount(request.getCustomerId(),
        		request.getAccountType().name(), request.getAccountSubType().name(),
        		request.getCurrency(), request.getNickname(), request.getDisplayName());
        
        Optional<Account> existing = repo.findByRequestFingerprint(fp);
        if (existing.isPresent()) {
        log.info("Idempotent replay for account create fp={}", fp);
        return mapper.toDto(existing.get());
        }
       
        Account account = mapper.toEntity(request);
        account.setAccountNumber(generateAccountNumber());
        account.setInstitutionId("CANADA001"); // Static for now; dynamic later
        account.setRequestFingerprint(fp);
        if (account.getLedgerBalance() == null) account.setLedgerBalance(new BigDecimal("0.00"));
        if (account.getAvailableBalance() == null) account.setAvailableBalance(account.getLedgerBalance());
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
    public HoldResponse placeHold(UUID accountId, CreateHoldRequest req, String idempotencyKey) {
    Account acc = repo.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
    BigDecimal amt = nonNullPositive(req.getAmount(), "amount");
    String currency = (req.getCurrency() == null || req.getCurrency().isBlank()) ? acc.getCurrency() : req.getCurrency();
    String fp = (idempotencyKey != null && !idempotencyKey.isBlank())
    ? idempotencyKey.trim()
    : Fingerprints.hold(accountId.toString(), amt.toPlainString(), currency, req.getType(), req.getReason());


    Optional<AccountHold> dup = holdRepo.findByAccountAndFingerprint(accountId, fp);
    if (dup.isPresent()) {
    return toHoldResponse(dup.get());
    }


    AccountHold hold = AccountHold.builder()
    .account(acc)
    .amount(amt)
    .currency(currency)
    .type(parseHoldType(req.getType()))
    .reason(req.getReason())
    .requestFingerprint(fp)
    .build();
    if (req.getReleaseAfterDays() != null && req.getReleaseAfterDays() > 0) {
    hold.setReleaseAt(LocalDateTime.now().plusDays(req.getReleaseAfterDays()));
    }
    hold = holdRepo.save(hold);


    // Update available balance snapshot for fast reads
    BigDecimal holds = holdRepo.sumByAccountAndStatus(accountId, HoldStatus.ACTIVE);
    acc.setAvailableBalance(acc.getLedgerBalance().subtract(holds));
    repo.save(acc);


    return toHoldResponse(hold);
    }
    
    
    @Transactional
    public void releaseHold(UUID accountId, Long holdId, Integer expectedVersion) {
    Account acc = repo.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
    if (expectedVersion != null && !expectedVersion.equals(acc.getVersion())) {
    throw new VersionMismatchException("If-Match version mismatch");
    }
    AccountHold hold = holdRepo.findById(holdId).orElseThrow(() -> new ResourceNotFoundException("Hold not found"));
    if (!acc.getId().equals(hold.getAccount().getId())) {
    throw new ConflictException("Hold does not belong to account");
    }
    if (hold.getStatus() == HoldStatus.RELEASED || hold.getStatus() == HoldStatus.CANCELED) {
    return; // idempotent
    }
    hold.setStatus(HoldStatus.RELEASED);
    holdRepo.save(hold);


    BigDecimal holds = holdRepo.sumByAccountAndStatus(accountId, HoldStatus.ACTIVE);
    acc.setAvailableBalance(acc.getLedgerBalance().subtract(holds));
    repo.save(acc);
    }
    
    
    
    // --------------------- Postings (credit/debit) ---------------------

    @Transactional
    public LedgerEntryResponse credit(UUID accountId, PostingRequest req, String idempotencyKey, Integer expectedVersion) {
        return post(accountId, AccountLedgerEntry.LedgerSide.CREDIT, req, idempotencyKey, expectedVersion);
    }

    @Transactional
    public LedgerEntryResponse debit(UUID accountId, PostingRequest req, String idempotencyKey, Integer expectedVersion) {
        return post(accountId, AccountLedgerEntry.LedgerSide.DEBIT, req, idempotencyKey, expectedVersion);
    }

    private LedgerEntryResponse post(UUID accountId, AccountLedgerEntry.LedgerSide side, PostingRequest req,
                                     String idempotencyKey, Integer expectedVersion) {
        Account acc = repo.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
        if (expectedVersion != null && !expectedVersion.equals(acc.getVersion())) {
            throw new VersionMismatchException("If-Match version mismatch");
        }
        BigDecimal amt = nonNullPositive(req.getAmount(), "amount");
        String currency = (req.getCurrency() == null || req.getCurrency().isBlank()) ? acc.getCurrency() : req.getCurrency();

        String fp = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? idempotencyKey.trim()
                : Fingerprints.posting(accountId.toString(), side.name(), amt.toPlainString(), currency, req.getDescription());

        Optional<AccountLedgerEntry> dup = ledgerRepo.findByAccountIdAndRequestFingerprint(accountId, fp);
        if (dup.isPresent()) {
            log.info("Idempotent replay for posting fp={}", fp);
            return toLedgerResponse(dup.get());
        }

        // Ledger math
        BigDecimal newLedger = acc.getLedgerBalance();
        if (side == AccountLedgerEntry.LedgerSide.CREDIT) {
            newLedger = newLedger.add(amt);
        } else {
            // On debit, ensure available (ledger − holds) >= amount
            BigDecimal activeHolds = holdRepo.sumByAccountAndStatus(accountId, HoldStatus.ACTIVE);
            BigDecimal available = newLedger.subtract(activeHolds == null ? BigDecimal.ZERO : activeHolds);
            if (available.compareTo(amt) < 0) {
                throw new PreconditionRequiredException("Insufficient available balance");
            }
            newLedger = newLedger.subtract(amt);
        }

        acc.setLedgerBalance(newLedger);
        BigDecimal activeHolds = holdRepo.sumByAccountAndStatus(accountId, HoldStatus.ACTIVE);
        acc.setAvailableBalance(newLedger.subtract(activeHolds == null ? BigDecimal.ZERO : activeHolds));
        repo.save(acc); // will bump @Version

        AccountLedgerEntry entry = AccountLedgerEntry.builder()
                .account(acc)
                .side(side)
                .amount(amt)
                .resultingLedgerBalance(newLedger)
                .description(req.getDescription())
                .requestFingerprint(fp)
                .build();
        entry = ledgerRepo.save(entry);

        return toLedgerResponse(entry);
    }


    


    
    
 // --------------------- Helpers ---------------------


    private HoldType parseHoldType(String t) {
    if (t == null) return HoldType.OTHER;
    try { return HoldType.valueOf(t.toUpperCase()); } catch (Exception e) { return HoldType.OTHER; }
    }


    private BigDecimal nonNullPositive(BigDecimal v, String field) {
    if (v == null || v.compareTo(BigDecimal.ZERO) <= 0) {
    throw new BadRequestException("Invalid " + field + ": must be > 0");
    }
    return v;
    }


    private LedgerEntryResponse toLedgerResponse(AccountLedgerEntry e) {
    return LedgerEntryResponse.builder()
    .id(e.getId())
    .side(e.getSide().name())
    .amount(e.getAmount())
    .resultingLedgerBalance(e.getResultingLedgerBalance())
    .postedAt(e.getPostedAt())
    .description(e.getDescription())
    .externalTransactionId(e.getExternalTransactionId())
    .build();
    }


    private HoldResponse toHoldResponse(AccountHold h) {
    return HoldResponse.builder()
    .id(h.getId())
    .amount(h.getAmount())
    .currency(h.getCurrency())
    .status(h.getStatus().name())
    .type(h.getType() == null ? null : h.getType().name())
    .reason(h.getReason())
    .releaseAt(h.getReleaseAt())
    .createdAt(h.getCreatedAt())
    .build();
    }
    
    
    private String generateAccountNumber() {
        return "CAN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
    
    
    
    
    
  
}
