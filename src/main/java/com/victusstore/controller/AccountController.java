package com.victusstore.controller;

import com.victusstore.model.Account;
import com.victusstore.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getAccountByEmail(@PathVariable String email) {
        return accountRepository.findByEmail(email)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body(Map.of("error", "Account not found")));
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        Account savedAccount = accountRepository.save(account);
        return ResponseEntity.ok(savedAccount);
    }

    @PutMapping("/{email}")
    public ResponseEntity<?> updateAccount(@PathVariable String email, @RequestBody Account accountDetails) {
        try {
            Optional<Account> accountOpt = accountRepository.findByEmail(email);

            if (accountOpt.isPresent()) {
                Account account = accountOpt.get();

                account.setFirstName(accountDetails.getFirstName());
                account.setLastName(accountDetails.getLastName());
                account.setPhoneNum(accountDetails.getPhoneNum());

                // update password only if provided
                if (accountDetails.getPassword() != null && !accountDetails.getPassword().isBlank()) {
                    account.setPassword(passwordEncoder.encode(accountDetails.getPassword()));
                }

                account.setIsActive(accountDetails.getIsActive());
                account.setSellerAccount(accountDetails.getSellerAccount());

                Account updatedAccount = accountRepository.save(account);
                return ResponseEntity.ok(updatedAccount);
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "Account not found"));
            }

        } catch (Exception e) {
            logger.error("Failed to update account {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to update account",
                    "details", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Map<String, Boolean>> deleteAccount(@PathVariable String email) {
        return accountRepository.findByEmail(email)
                .map(account -> {
                    accountRepository.delete(account);
                    Map<String, Boolean> response = new HashMap<>();
                    response.put("deleted", Boolean.TRUE);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
