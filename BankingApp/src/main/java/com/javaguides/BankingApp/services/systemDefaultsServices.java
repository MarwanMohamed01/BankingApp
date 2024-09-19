package com.javaguides.BankingApp.services;

import com.javaguides.BankingApp.system.systemDefaultsRepository;
import com.javaguides.BankingApp.system.systemDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Log4j2
@Service
public class systemDefaultsServices {

    @Autowired
    private systemDefaultsRepository systemDefaultsRepository;

    public void setTransactionFee() {
        // Check if a record with key "transaction fees" already exists
        Optional<systemDefaults> existingFee = systemDefaultsRepository.findByKey("Transaction Fees");
        if (!existingFee.isPresent()) {
            systemDefaults transactionFee = new systemDefaults("Transaction Fees", 0.1);
            systemDefaultsRepository.save(transactionFee);
            log.info("Transaction fee set to {} and saved to database.", transactionFee.getValue());
        } else {
            log.info("Transaction fee already exists with value: {}", existingFee.get().getValue());
        }
    }

    public double getTransactionFee() {
        // Retrieve the transaction fee from the database
        Optional<systemDefaults> transactionFee = systemDefaultsRepository.findByKey("Transaction Fees");
        double fee = transactionFee.map(systemDefaults::getValue).orElse(0.0);
        log.info("Retrieved transaction fee: {}", fee);
        return fee;
    }
}
