package com.javaguides.BankingApp.wallet;

import com.javaguides.BankingApp.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Integer> {

    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.user = :user AND w.isDeleted = false")
    long countByUser(@Param("user") User user);

    Optional<Wallet> findByIbanAndIsDeletedFalse(String iban);

    @Query("SELECT w FROM Wallet w WHERE w.user = :user AND w.type = :type AND w.isDeleted = false")
    Optional<Wallet> findByUserAndType(@Param("user") User user, @Param("type") WalletType type);

    @Query("SELECT w FROM Wallet w WHERE w.id = :id AND w.isDeleted = false")
    Optional<Wallet> findByIdAndIsDeletedFalse(@Param("id") int id);
}
