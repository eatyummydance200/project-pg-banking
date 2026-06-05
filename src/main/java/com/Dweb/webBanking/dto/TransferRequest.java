package com.Dweb.webBanking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull(message = "fromAccountId는 필수입니다.")
        Long fromAccountId,
        @NotNull(message = "toAccountId는 필수입니다.")
        Long toAccountId,
        @NotNull(message = "amount는 필수입니다.")
        @DecimalMin(value = "0.01", inclusive = true, message = "amount는 0보다 커야 합니다.")
        BigDecimal amount
) {
}
