package com.javaguides.BankingApp.walletLogs;

import com.javaguides.BankingApp.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface depositWithdarwRepository extends JpaRepository<depositWithdraw, Integer> {
    List<depositWithdraw> findByWallet_User(User user);
}
