package com.javaguides.BankingApp.services;

import com.javaguides.BankingApp.services.emailService;
import com.javaguides.BankingApp.user.User;
import com.javaguides.BankingApp.wallet.Wallet;
import com.javaguides.BankingApp.wallet.WalletRepository;
import com.javaguides.BankingApp.wallet.WalletType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class walletServices {

    private final WalletRepository walletRepository;
    private final emailService emailService;
    private final depositWithdrawServices depositWithdrawServices;

    public void createWallet(User user) {
        log.info("Creating new wallet for UserID={} with default type Local", user.getId());
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setType(WalletType.Local);
        wallet.setBalance(0);

        walletRepository.save(wallet);
        log.info("Wallet created and saved: UserID={}, WalletID={}", user.getId(), wallet.getId());
    }

    public void createNewWallet(User user, WalletType type) {
        log.info("Attempting to create a new wallet for UserID={} with type {}", user.getId(), type);
        Optional<Wallet> existingWallet = walletRepository.findByUserAndType(user, type);

        if (existingWallet.isPresent()) {
            log.error("User already has a wallet of type: {} - WalletID={}", type, existingWallet.get().getId());
            throw new IllegalArgumentException("User already has a wallet of type: " + type);
        }

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setType(type);
        wallet.setBalance(0);

        walletRepository.save(wallet);
        log.info("New wallet created and saved: UserID={}, WalletID={}", user.getId(), wallet.getId());
    }

    public boolean deleteWallet(User user, int walletId) {
        log.info("Attempting to delete WalletID={} for UserID={}", walletId, user.getId());

        long walletCount = walletRepository.countByUser(user);
        if (walletCount < 2) {
            log.warn("User has fewer than 2 wallets. Deletion not allowed: UserID={}", user.getId());
            return false;
        }

        Optional<Wallet> walletOptional = walletRepository.findByIdAndIsDeletedFalse(walletId);
        if (walletOptional.isPresent()) {
            Wallet wallet = walletOptional.get();
            if (wallet.getUser().equals(user)) {
                wallet.setDeleted(true);
                walletRepository.save(wallet);
                log.info("Wallet marked as deleted: WalletID={}", walletId);
                return true;
            }
        }

        log.error("Wallet not found, already deleted, or does not belong to the user: WalletID={}", walletId);
        return false;
    }

    public boolean updateWalletType(User user, int walletId, WalletType newType) {
        log.info("Attempting to update WalletID={} for UserID={} to new type {}", walletId, user.getId(), newType);

        Optional<Wallet> walletOptional = walletRepository.findByIdAndIsDeletedFalse(walletId);
        if (walletOptional.isPresent()) {
            Wallet wallet = walletOptional.get();
            if (wallet.getUser().equals(user)) {
                Optional<Wallet> existingWallet = walletRepository.findByUserAndType(user, newType);
                if (existingWallet.isPresent() && existingWallet.get().getId() != walletId) {
                    log.error("User already has a wallet of the new type: UserID={}, NewType={}", user.getId(), newType);
                    return false;
                }

                wallet.setType(newType);
                walletRepository.save(wallet);
                log.info("Wallet type updated successfully: WalletID={}, NewType={}", walletId, newType);
                return true;
            }
        }

        log.error("Wallet not found, already deleted, or does not belong to the user: WalletID={}", walletId);
        return false;
    }

    public boolean depositToWallet(User user, int walletId, int amount) {
        log.info("Attempting to deposit {} to WalletID={} for UserID={}", amount, walletId, user.getId());

        if (amount <= 0) {
            log.warn("Invalid deposit amount: {}", amount);
            return false;
        }

        Optional<Wallet> walletOptional = walletRepository.findByIdAndIsDeletedFalse(walletId);
        if (walletOptional.isPresent()) {
            Wallet wallet = walletOptional.get();
            if (wallet.getUser().equals(user)) {
                wallet.setBalance(wallet.getBalance() + amount);
                walletRepository.save(wallet);
                depositWithdrawServices.saveDeposit(user, amount, wallet);
                emailService.sendDepositNotification(user.getEmail(), user.getFirstName(), amount, wallet.getType());
                log.info("Deposit successful: UserID={}, WalletID={}, Amount={}", user.getId(), walletId, amount);
                return true;
            }
        }

        log.error("Wallet not found, already deleted, or does not belong to the user: WalletID={}", walletId);
        return false;
    }

    public boolean withdrawFromWallet(User user, int walletId, int amount) {
        log.info("Attempting to withdraw {} from WalletID={} for UserID={}", amount, walletId, user.getId());

        if (amount <= 0) {
            log.warn("Invalid withdraw amount: {}", amount);
            return false;
        }

        Optional<Wallet> walletOptional = walletRepository.findByIdAndIsDeletedFalse(walletId);
        if (walletOptional.isPresent()) {
            Wallet wallet = walletOptional.get();
            if (wallet.getUser().equals(user)) {
                if (wallet.getBalance() >= amount) {
                    wallet.setBalance(wallet.getBalance() - amount);
                    walletRepository.save(wallet);
                    depositWithdrawServices.saveWithdraw(user, amount, wallet);
                    log.info("Withdrawal successful: UserID={}, WalletID={}, Amount={}", user.getId(), walletId, amount);
                    return true;
                } else {
                    log.warn("Insufficient balance for withdrawal: UserID={}, WalletID={}, RequestedAmount={}, CurrentBalance={}",
                            user.getId(), walletId, amount, wallet.getBalance());
                    return false; // Insufficient balance
                }
            }
        }

        log.error("Wallet not found, already deleted, or does not belong to the user: WalletID={}", walletId);
        return false;
    }
}
