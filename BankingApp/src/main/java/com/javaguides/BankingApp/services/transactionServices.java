package com.javaguides.BankingApp.services;

import com.javaguides.BankingApp.transaction.Transaction;
import com.javaguides.BankingApp.transaction.TransactionDTO;
import com.javaguides.BankingApp.transaction.TransactionRepository;
import com.javaguides.BankingApp.transaction.TransactionStatus;
import com.javaguides.BankingApp.wallet.Wallet;
import com.javaguides.BankingApp.wallet.WalletRepository;
import com.javaguides.BankingApp.user.User;
import com.javaguides.BankingApp.wallet.WalletType;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
public class transactionServices {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private systemDefaultsServices systemDefaultsServices;

    @Autowired
    private emailService emailService;

    public double sendMoney(User sender, String receiverIban, double amount, WalletType walletType) {
        log.info("Initiating money transfer: Sender={}, ReceiverIban={}, Amount={}, WalletType={}",
                sender.getId(), receiverIban, amount, walletType);

        Wallet senderWallet = walletRepository.findByUserAndType(sender, walletType)
                .orElseThrow(() -> {
                    log.error("Sender's wallet not found for UserID={} and WalletType={}", sender.getId(), walletType);
                    return new IllegalArgumentException("Sender's wallet not found");
                });

        Wallet receiverWallet = walletRepository.findByIbanAndIsDeletedFalse(receiverIban)
                .orElseThrow(() -> {
                    log.error("Receiver's wallet not found for Iban={}", receiverIban);
                    return new IllegalArgumentException("Receiver's wallet not found");
                });

        if (!senderWallet.getType().equals(receiverWallet.getType())) {
            log.error("Wallet types do not match for SenderID={} and ReceiverIban={}", sender.getId(), receiverIban);
            throw new IllegalArgumentException("Wallet types do not match. Transfer not allowed.");
        }

        double transactionFee = systemDefaultsServices.getTransactionFee();
        double feeAmount = amount * transactionFee;
        double netAmount = amount - feeAmount;

        if (senderWallet.getBalance() >= amount) {
            senderWallet.setBalance(senderWallet.getBalance() - amount);
            receiverWallet.setBalance(receiverWallet.getBalance() + netAmount);

            walletRepository.save(senderWallet);
            walletRepository.save(receiverWallet);

            Transaction transaction = new Transaction();
            transaction.setSenderWallet(senderWallet);
            transaction.setReceiverWallet(receiverWallet);
            transaction.setAmount(amount);
            transaction.setWalletType(walletType);
            transaction.setStatus(TransactionStatus.SUCCESS);
            transactionRepository.save(transaction);

            log.info("Money transfer successful: SenderID={}, ReceiverID={}, Amount={}, NetAmount={}",
                    sender.getId(), receiverWallet.getUser().getId(), amount, netAmount);

            emailService.sendTransferNotification(sender.getEmail(), sender.getFirstName(), amount, walletType, receiverWallet.getUser().getFirstName());
            emailService.receiveTransferNotification(receiverWallet.getUser().getEmail(), sender.getFirstName(), netAmount, walletType, receiverWallet.getUser().getFirstName());

            return netAmount;
        } else {
            log.error("Insufficient balance for SenderID={} - Balance={}, RequestedAmount={}",
                    sender.getId(), senderWallet.getBalance(), amount);
            throw new IllegalArgumentException("Insufficient balance in sender's wallet");
        }
    }

    public List<TransactionDTO> getAllTransactionHistory(User user) {
        log.info("Fetching transaction history for UserID={}", user.getId());

        List<Transaction> transactions = transactionRepository.findBySenderWallet_UserOrReceiverWallet_User(user, user);

        return transactions.stream().map(transaction -> {
            TransactionDTO dto = new TransactionDTO();
            dto.setAmount(transaction.getAmount());
            dto.setTimestamp(transaction.getTimestamp());

            if (transaction.getSenderWallet().getUser().getId() == user.getId()) {
                // Sent transaction
                dto.setTransactionType("SENT");
                dto.setSenderWalletId(transaction.getSenderWallet().getId());
                dto.setReceiverName(transaction.getReceiverWallet().getUser().getFirstName());
                dto.setReceiverWalletId(transaction.getReceiverWallet().getId());
            } else if (transaction.getReceiverWallet().getUser().getId() == user.getId()) {
                // Received transaction
                dto.setTransactionType("RECEIVED");
                dto.setReceiverWalletId(transaction.getReceiverWallet().getId()); // This user is the receiver
                dto.setSenderName(transaction.getSenderWallet().getUser().getFirstName());
                dto.setSenderWalletId(transaction.getSenderWallet().getId());
            }
            return dto;
        }).collect(Collectors.toList());
    }
}
