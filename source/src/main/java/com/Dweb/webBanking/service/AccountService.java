package com.Dweb.webBanking.service;

import com.Dweb.webBanking.domain.Account;
import com.Dweb.webBanking.domain.AccountStatus;
import com.Dweb.webBanking.domain.BankTransaction;
import com.Dweb.webBanking.domain.BankUser;
import com.Dweb.webBanking.domain.TransactionType;
import com.Dweb.webBanking.dto.BalanceResponse;
import com.Dweb.webBanking.dto.CreateUserAccountRequest;
import com.Dweb.webBanking.dto.CreateUserAccountResponse;
import com.Dweb.webBanking.repository.AccountRepository;
import com.Dweb.webBanking.repository.BankTransactionRepository;
import com.Dweb.webBanking.repository.BankUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final BankUserRepository bankUserRepository;
    private final AccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;

    @Transactional
    public CreateUserAccountResponse createUserWithAccount(CreateUserAccountRequest request) {
        bankUserRepository.findByLoginId(request.loginId())
                .ifPresent(user -> {
                    throw new IllegalArgumentException("이미 사용 중인 로그인 ID입니다.");
                });

        BankUser user = bankUserRepository.save(BankUser.create(request.loginId(), request.name()));
        Account account = accountRepository.save(
                Account.create(user, generateAccountNumber(), request.initialBalance())
        );

        if (request.initialBalance().signum() > 0) {
            saveTransaction(account, TransactionType.DEPOSIT, request.initialBalance(), null, "초기 입금");
        }

        return new CreateUserAccountResponse(
                user.getId(),
                account.getId(),
                account.getAccountNumber(),
                account.getBalance()
        );
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("계좌가 없습니다."));

        return new BalanceResponse(account.getId(), account.getAccountNumber(), account.getBalance());
    }

    @Transactional
    public BalanceResponse deposit(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new NoSuchElementException("계좌가 없습니다."));

        validateActive(account);
        validateAmount(amount, "입금");

        account.deposit(amount);
        saveTransaction(account, TransactionType.DEPOSIT, amount, null, "일반 입금");

        return new BalanceResponse(account.getId(), account.getAccountNumber(), account.getBalance());
    }

    @Transactional
    public BalanceResponse withdraw(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new NoSuchElementException("계좌가 없습니다."));

        validateActive(account);
        validateAmount(amount, "출금");

        account.withdraw(amount);
        saveTransaction(account, TransactionType.WITHDRAW, amount, null, "일반 출금");

        return new BalanceResponse(account.getId(), account.getAccountNumber(), account.getBalance());
    }

    private void validateAmount(BigDecimal amount, String type) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(type + " 금액은 0보다 커야 합니다.");
        }
    }

    private void validateActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("활성 상태의 계좌만 처리할 수 있습니다.");
        }
    }

    private void saveTransaction(
            Account account,
            TransactionType type,
            BigDecimal amount,
            Long counterpartyId,
            String description
    ) {
        transactionRepository.save(BankTransaction.builder()
                .account(account)
                .txType(type)
                .amount(amount)
                .balanceAfter(account.getBalance())
                .counterpartyAccountId(counterpartyId)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build());
    }

    private String generateAccountNumber() {
        return "100-%d-%03d".formatted(
                System.currentTimeMillis(),
                ThreadLocalRandom.current().nextInt(1000)
        );
    }
}
