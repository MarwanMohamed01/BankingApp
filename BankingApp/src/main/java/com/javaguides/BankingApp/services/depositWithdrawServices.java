package com.javaguides.BankingApp.services;

import com.javaguides.BankingApp.user.User;
import com.javaguides.BankingApp.wallet.Wallet;
import com.javaguides.BankingApp.wallet.WalletRepository;
import com.javaguides.BankingApp.walletLogs.TransactionType;
import com.javaguides.BankingApp.walletLogs.depositWithdarwRepository;
import com.javaguides.BankingApp.walletLogs.depositWithdraw;
import com.javaguides.BankingApp.walletLogs.depositWithdrawDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
public class depositWithdrawServices {

    @Autowired
    private depositWithdarwRepository depositWithdarwRepository;

    @Autowired
    private WalletRepository walletRepository;

    public void saveDeposit(User user, double amount, Wallet wallet) {
        log.info("Initiating deposit of amount: {} for user: {} in wallet: {}", amount, user.getUsername(), wallet.getId());

        depositWithdraw deposit = new depositWithdraw();
        deposit.setType(TransactionType.Deposit);
        deposit.setAmount(amount);
        deposit.setWallet(wallet);
        deposit.setTimestamp(LocalDateTime.now());

        depositWithdarwRepository.save(deposit);

        log.info("Deposit saved successfully for wallet: {}", wallet.getId());
    }

    public void saveWithdraw(User user, double amount, Wallet wallet) {
        log.info("Initiating withdrawal of amount: {} for user: {} in wallet: {}", amount, user.getUsername(), wallet.getId());

        depositWithdraw withdraw = new depositWithdraw();
        withdraw.setType(TransactionType.Withdraw);
        withdraw.setAmount(amount);
        withdraw.setWallet(wallet);
        withdraw.setTimestamp(LocalDateTime.now());

        depositWithdarwRepository.save(withdraw);

        log.info("Withdrawal saved successfully for wallet: {}", wallet.getId());
    }

    public List<depositWithdrawDTO> getDepositWithdrawHistory(User user) {
        log.info("Fetching deposit/withdraw history for user: {}", user.getUsername());

        List<depositWithdraw> history = depositWithdarwRepository.findByWallet_User(user);

        log.debug("Found {} records of deposit/withdraw for user: {}", history.size(), user.getUsername());

        return history.stream().map(d -> {
            depositWithdrawDTO dto = new depositWithdrawDTO();
//            dto.setWalletId(d.getWallet().getId());
            dto.setWalletType(d.getWallet().getType());
            dto.setAmount(d.getAmount());
            dto.setType(d.getType().name());
            dto.setTimestamp(d.getTimestamp());
            return dto;
        }).collect(Collectors.toList());
    }
}
