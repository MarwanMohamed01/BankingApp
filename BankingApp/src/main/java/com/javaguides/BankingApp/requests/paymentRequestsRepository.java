package com.javaguides.BankingApp.requests;

import com.javaguides.BankingApp.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface paymentRequestsRepository extends JpaRepository<paymentRequests,Integer> {
    List<paymentRequests> findByRequestedUserAndStatus(User requestedUser, requestStatus status);
}
