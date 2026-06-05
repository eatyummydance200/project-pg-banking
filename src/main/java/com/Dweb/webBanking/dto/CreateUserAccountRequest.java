package com.Dweb.webBanking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateUserAccountRequest(
        @NotBlank(message = "loginId는 필수입니다.")
        @Size(max = 50, message = "loginId는 50자를 넘길 수 없습니다.")
        String loginId,
        @NotBlank(message = "name은 필수입니다.")
        @Size(max = 100, message = "name은 100자를 넘길 수 없습니다.")
        String name,
        @NotNull(message = "initialBalance는 필수입니다.")
        @DecimalMin(value = "0.00", inclusive = true, message = "initialBalance는 0 이상이어야 합니다.")
        BigDecimal initialBalance
) {
}
