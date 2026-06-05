package com.Dweb.webBanking.controller;

import com.Dweb.webBanking.dto.AmountRequest;
import com.Dweb.webBanking.dto.BalanceResponse;
import com.Dweb.webBanking.dto.CreateUserAccountRequest;
import com.Dweb.webBanking.dto.CreateUserAccountResponse;
import com.Dweb.webBanking.dto.TransferRequest;
import com.Dweb.webBanking.dto.TransferResponse;
import com.Dweb.webBanking.service.AccountService;
import com.Dweb.webBanking.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BankingController {

    private final AccountService accountService;
    private final TransferService transferService;

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserAccountResponse createUserWithAccount(@Valid @RequestBody CreateUserAccountRequest request) {
        return accountService.createUserWithAccount(request);
    }

    @GetMapping("/accounts/{accountId}/balance")
    public BalanceResponse getBalance(@PathVariable Long accountId) {
        return accountService.getBalance(accountId);
    }

    @PostMapping("/accounts/{accountId}/deposit")
    public BalanceResponse deposit(@PathVariable Long accountId, @Valid @RequestBody AmountRequest request) {
        return accountService.deposit(accountId, request.amount());
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    public BalanceResponse withdraw(@PathVariable Long accountId, @Valid @RequestBody AmountRequest request) {
        return accountService.withdraw(accountId, request.amount());
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request) {
        UUID transferId = transferService.transfer(
                request.fromAccountId(),
                request.toAccountId(),
                request.amount()
        );

        return new TransferResponse(
                transferId,
                request.fromAccountId(),
                request.toAccountId(),
                request.amount()
        );
    }
}
