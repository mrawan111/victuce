package com.victusstore.controller;

import com.victusstore.model.Account;
import com.victusstore.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{email}")
    public ResponseEntity<Account> getAccountByEmail(@PathVariable String email) {
        return accountRepository.findByEmail(email)
            .map(account -> ResponseEntity.ok(account))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        Account savedAccount = accountRepository.save(account);
        return ResponseEntity.ok(savedAccount);
    }

    @PutMapping("/{email}")
    public ResponseEntity<Account> updateAccount(@PathVariable String email, @RequestBody Account accountDetails) {
        return accountRepository.findByEmail(email)
            .map(account -> {
                account.setFirstName(accountDetails.getFirstName());
                account.setLastName(accountDetails.getLastName());
                account.setEmail(accountDetails.getEmail());
                account.setPhoneNum(accountDetails.getPhoneNum());
                account.setPassword(passwordEncoder.encode(accountDetails.getPassword()));
                account.setIsActive(accountDetails.getIsActive());
                account.setSellerAccount(accountDetails.getSellerAccount());
                Account updatedAccount = accountRepository.save(account);
                return ResponseEntity.ok(updatedAccount);
            })
            .orElse(ResponseEntity.notFound().build());
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
