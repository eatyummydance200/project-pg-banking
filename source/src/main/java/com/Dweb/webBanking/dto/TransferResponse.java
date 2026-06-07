package com.Dweb.webBanking.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferResponse(
        UUID transferId,
        Long fromAccountId,
        Long toAccountId,
        BigDecimal amount
) {
}
