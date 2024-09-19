package com.javaguides.BankingApp.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class paymentRequestDTO {
    private int id;              // Add this line
    private String requesterName;
    private double amount;
    private requestStatus status;
    private LocalDateTime time;
}
