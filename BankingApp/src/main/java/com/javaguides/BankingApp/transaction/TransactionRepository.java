package com.javaguides.BankingApp.transaction;

import com.javaguides.BankingApp.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findBySenderWallet_UserOrReceiverWallet_User(User sender, User receiver);

    List<Transaction> findBySenderWallet_UserAndReceiverWalletIsNull(User user); // For deposits
    List<Transaction> findByReceiverWallet_UserAndSenderWalletIsNull(User user); // For withdrawals
}


