package com.Dweb.webBanking.repository;

import com.Dweb.webBanking.domain.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
}