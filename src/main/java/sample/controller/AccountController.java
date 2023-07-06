package sample.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sample.model.account.Account;
import sample.usecase.AccountService;

/**
 * Processes customer UI requests for accounts.
 */
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService service;

    @GetMapping("")
    public Account loadAccount() {
        return this.service.loadAccount();
    }

}
