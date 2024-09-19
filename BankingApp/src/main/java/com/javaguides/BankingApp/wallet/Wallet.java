package com.javaguides.BankingApp.wallet;


import com.javaguides.BankingApp.transaction.Transaction;
import com.javaguides.BankingApp.user.User;
import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "wallet")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private WalletType type;

    private double balance;

    @Column(unique = true, nullable = false)
    private String iban;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isDeleted = false;



    public Wallet() {
        this.iban = UUID.randomUUID().toString();
    }

}
