package com.Dweb.webBanking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AmountRequest(
        @NotNull(message = "amount는 필수입니다.")
        @DecimalMin(value = "0.01", inclusive = true, message = "amount는 0보다 커야 합니다.")
        BigDecimal amount
) {
}
