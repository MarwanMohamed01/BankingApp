package com.javaguides.BankingApp.controllers;

import com.javaguides.BankingApp.wallet.WalletType;
import com.javaguides.BankingApp.services.walletServices;
import com.javaguides.BankingApp.user.User;
import com.javaguides.BankingApp.user.UserRepository;
import com.javaguides.BankingApp.wallet.walletRequest;
import com.javaguides.BankingApp.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class walletController {

    private final walletServices walletServices;
    private final UserRepository userRepository;

    @PostMapping("/create-wallet")
    public ResponseEntity<ApiResponse<Void>> createAdditionalWallet(@RequestBody walletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("error", "User is not authenticated", null));
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("error", "User not found", null));
        }

        try {
            walletServices.createNewWallet(user, request.getType());
            return ResponseEntity.ok(
                    new ApiResponse<>("success", "Additional wallet created successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @DeleteMapping("/delete-wallet/{walletId}")
    public ResponseEntity<ApiResponse<Void>> deleteWallet(@PathVariable int walletId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("error", "User is not authenticated", null));
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("error", "User not found", null));
        }

        boolean success = walletServices.deleteWallet(user, walletId);
        if (success) {
            return ResponseEntity.ok(
                    new ApiResponse<>("success", "Wallet deleted successfully", null));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>("error", "Cannot delete wallet: Ensure you have more than 2 wallets and the wallet exists", null));
        }
    }

    @PutMapping("/update-wallet-type/{walletId}")
    public ResponseEntity<ApiResponse<Void>> updateWalletType(
            @PathVariable int walletId,
            @RequestBody WalletType newType) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("error", "User is not authenticated", null));
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("error", "User not found", null));
        }

        boolean success = walletServices.updateWalletType(user, walletId, newType);
        if (success) {
            return ResponseEntity.ok(
                    new ApiResponse<>("success", "Wallet type updated successfully", null));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>("error", "Cannot update wallet type: Ensure the wallet exists and belongs to the user", null));
        }
    }

    @PostMapping("/deposit/{walletId}")
    public ResponseEntity<ApiResponse<Integer>> depositToWallet(
            @PathVariable int walletId,
            @RequestParam int amount) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("error", "User is not authenticated", null));
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("error", "User not found", null));
        }

        boolean success = walletServices.depositToWallet(user, walletId, amount);
        if (success) {
            return ResponseEntity.ok(
                    new ApiResponse<>("success", "Deposit successful", amount));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>("error", "Cannot deposit: Ensure the amount is positive and the wallet exists and belongs to the user", null));
        }
    }

    @PostMapping("/withdraw/{walletId}")
    public ResponseEntity<ApiResponse<Integer>> withdrawFromWallet(
            @PathVariable int walletId,
            @RequestParam int amount) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("error", "User is not authenticated", null));
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("error", "User not found", null));
        }

        boolean success = walletServices.withdrawFromWallet(user, walletId, amount);
        if (success) {
            return ResponseEntity.ok(
                    new ApiResponse<>("success", "Withdrawal successful", amount));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>("error", "Cannot withdraw: Ensure the amount is positive, does not exceed the balance, and the wallet exists and belongs to the user", null));
        }
    }
}
