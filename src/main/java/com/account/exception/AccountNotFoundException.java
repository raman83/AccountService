package com.account.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String accountId) {
        super("No account with ID: " + accountId);
    }
}
