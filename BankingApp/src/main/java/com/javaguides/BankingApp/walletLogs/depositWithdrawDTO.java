package com.javaguides.BankingApp.walletLogs;

import com.javaguides.BankingApp.wallet.WalletType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class depositWithdrawDTO {
//    private Integer walletId;
    private WalletType walletType;
    private double amount;
    private String type;
    private LocalDateTime timestamp;
}
