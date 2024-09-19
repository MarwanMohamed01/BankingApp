package com.javaguides.BankingApp.controllers;

import com.javaguides.BankingApp.requests.paymentRequestDTO;
import com.javaguides.BankingApp.requests.paymentRequests;
import com.javaguides.BankingApp.requests.requestStatus;
import com.javaguides.BankingApp.services.paymentRequestsServices;
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
@RequestMapping("/api/v1/payment-requests")
@RequiredArgsConstructor
public class paymentRequestController {

    private final paymentRequestsServices paymentRequestsServices;
    private final UserRepository userRepository;

    // API to request a payment from another user
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<String>> requestPayment(
            @RequestParam("requestedUserId") int requestedUserId,
            @RequestParam("amount") double amount) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("error", "User not authenticated", null));
        }

        // Get the requester (user A)
        String requesterEmail = authentication.getName();
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalArgumentException("Requester not found"));

        // Get the requested user (user B) by ID
        User requestedUser = userRepository.findById(requestedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Requested user not found"));

        // Request payment
        paymentRequestsServices.requestPayment(requester, requestedUser, amount);

        return ResponseEntity.ok(new ApiResponse<>("success", "Payment request sent", null));
    }

    // API to get all pending payment requests for the logged-in user (user B)
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<paymentRequestDTO>>> getPendingRequests() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("error", "User not authenticated", null));
        }

        String email = authentication.getName();
        User requestedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Requested user not found"));

        // Fetch pending requests for the user
        List<paymentRequestDTO> pendingRequests = paymentRequestsServices.getPendingRequests(requestedUser);

        return ResponseEntity.ok(new ApiResponse<>("success", "Pending requests retrieved", pendingRequests));
    }

    // API to accept or reject a payment request
    @PostMapping("/process")
    public ResponseEntity<ApiResponse<String>> processRequest(
            @RequestParam("requestId") int requestId,
            @RequestParam("accept") boolean accept,
            @RequestParam("walletType") WalletType walletType) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("error", "User not authenticated", null));
        }

        // Get the requested user (user B)
        String email = authentication.getName();
        User requestedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Requested user not found"));

        // Process the payment request (accept or reject)
        paymentRequestsServices.processRequest(requestId, accept, requestedUser, walletType);

        String message = accept ? "Payment request accepted" : "Payment request rejected";
        return ResponseEntity.ok(new ApiResponse<>("success", message, null));
    }
}
