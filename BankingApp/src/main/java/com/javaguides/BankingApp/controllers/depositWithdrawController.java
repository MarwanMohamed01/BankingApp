package com.javaguides.BankingApp.controllers;

import com.javaguides.BankingApp.services.depositWithdrawServices;
import com.javaguides.BankingApp.user.User;
import com.javaguides.BankingApp.user.UserRepository;
import com.javaguides.BankingApp.utils.ApiResponse;
import com.javaguides.BankingApp.walletLogs.depositWithdrawDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/deposit-withdraw")
@RequiredArgsConstructor
public class depositWithdrawController {

    private final depositWithdrawServices depositWithdrawService;
    private final UserRepository userRepository;

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<depositWithdrawDTO>>> getDepositWithdrawHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<depositWithdrawDTO> history = depositWithdrawService.getDepositWithdrawHistory(user);

        return ResponseEntity.ok(new ApiResponse<>("success", "Transaction history fetched", history));
    }
}
