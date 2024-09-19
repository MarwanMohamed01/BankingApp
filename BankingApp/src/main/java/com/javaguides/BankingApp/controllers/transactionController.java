package com.javaguides.BankingApp.controllers;
import com.javaguides.BankingApp.services.transactionServices;
import com.javaguides.BankingApp.transaction.TransactionDTO;
import com.javaguides.BankingApp.user.User;
import com.javaguides.BankingApp.user.UserRepository;
import com.javaguides.BankingApp.utils.ApiResponse;
import com.javaguides.BankingApp.wallet.WalletType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class transactionController {

    private final transactionServices transactionService;
    private final UserRepository userRepository;
//    private final depositWithdrawDTO depositWithdrawDTO;
//    @Autowired
//    private depositWithdrawServices depositWithdrawServices;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Double>> sendMoney(
            @RequestParam String receiverIban,
            @RequestParam double amount,
            @RequestParam WalletType walletType) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("error", "User not authenticated", null));
        }

        String email = authentication.getName();
        User sender = userRepository.findByEmail(email).orElse(null);

        if (sender == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("error", "User not found", null));
        }

        try {
            double amountSent = transactionService.sendMoney(sender, receiverIban, amount, walletType);
            return ResponseEntity.ok(
                    new ApiResponse<>("success", "Transaction successful", amountSent));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getAllTransactionHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("error", "User not authenticated", null));
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("error", "User not found", null));
        }

        List<TransactionDTO> transactionHistory = transactionService.getAllTransactionHistory(user);
//        List<depositWithdrawDTO> history = depositWithdrawServices.getDepositWithdrawHistory(user);
        return ResponseEntity.ok(
                new ApiResponse<>("success", "Transaction history retrieved", transactionHistory));
    }


}
