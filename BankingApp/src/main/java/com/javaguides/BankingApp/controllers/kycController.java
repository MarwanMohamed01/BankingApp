package com.javaguides.BankingApp.controllers;

import com.javaguides.BankingApp.services.emailService;
import com.javaguides.BankingApp.services.kycService;
import com.javaguides.BankingApp.user.User;
import com.javaguides.BankingApp.user.UserRepository;
import com.javaguides.BankingApp.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
public class kycController {

    private final kycService kycService;
    private final UserRepository userRepository;
    private final emailService emailService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadKycDocument(
            @RequestParam("file") MultipartFile file) {

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

        try {
            // Save the file and update the user's KYC status
            kycService.uploadKycDocument(user, file);

            // Send verification notification
            emailService.sendVerificationNotification(user.getEmail(), user.getFirstName());

            return ResponseEntity.ok(new ApiResponse<>("success", "KYC document uploaded and verified", user.getKycDocumentPath()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>("error", "Error processing KYC document", null));
        }
    }
}
