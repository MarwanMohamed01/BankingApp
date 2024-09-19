package com.javaguides.BankingApp.transaction;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Setter
@Getter
public class TransactionDTO {
    private double amount;
    private String transactionType;
//    private int walletId;
    private Integer senderWalletId;
    private Integer receiverWalletId;
    private String senderName;
    private String receiverName;
    private LocalDateTime timestamp;
}
