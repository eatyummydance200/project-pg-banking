package com.Dweb.webBanking.service;

import com.Dweb.webBanking.domain.Account;
import com.Dweb.webBanking.domain.AccountStatus;
import com.Dweb.webBanking.domain.BankTransaction;
import com.Dweb.webBanking.domain.TransactionType;
import com.Dweb.webBanking.repository.AccountRepository;
import com.Dweb.webBanking.repository.BankTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UUID transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("같은 계좌로 이체할 수 없습니다.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("이체 금액은 0보다 커야 합니다.");
        }

        List<Long> lockIds = List.of(
                Math.min(fromAccountId, toAccountId),
                Math.max(fromAccountId, toAccountId)
        );

        List<Account> locked = accountRepository.findAllByIdForUpdate(lockIds);
        if (locked.size() != 2) {
            throw new NoSuchElementException("계좌를 찾을 수 없습니다.");
        }

        Map<Long, Account> accountMap = locked.stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));

        Account from = accountMap.get(fromAccountId);
        Account to = accountMap.get(toAccountId);

        if (from.getStatus() != AccountStatus.ACTIVE || to.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("비활성 계좌는 이체할 수 없습니다.");
        }

        from.withdraw(amount);
        to.deposit(amount);

        UUID transferId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        transactionRepository.save(BankTransaction.builder()
                .account(from)
                .txType(TransactionType.TRANSFER_OUT)
                .amount(amount)
                .balanceAfter(from.getBalance())
                .counterpartyAccountId(to.getId())
                .transferId(transferId)
                .description("계좌이체 출금")
                .createdAt(now)
                .build());

        transactionRepository.save(BankTransaction.builder()
                .account(to)
                .txType(TransactionType.TRANSFER_IN)
                .amount(amount)
                .balanceAfter(to.getBalance())
                .counterpartyAccountId(from.getId())
                .transferId(transferId)
                .description("계좌이체 입금")
                .createdAt(now)
                .build());

        return transferId;
    }
}
