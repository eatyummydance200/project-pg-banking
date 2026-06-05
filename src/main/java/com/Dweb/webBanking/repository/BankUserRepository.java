package com.Dweb.webBanking.repository;

import com.Dweb.webBanking.domain.BankUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankUserRepository extends JpaRepository<BankUser, Long> {
    Optional<BankUser> findByLoginId(String loginId);
}
