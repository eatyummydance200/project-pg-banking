package com.Dweb.webBanking.dto;

import java.math.BigDecimal;

public record CreateUserAccountResponse(
        Long userId,
        Long accountId,
        String accountNumber,
        BigDecimal balance
) {
}
