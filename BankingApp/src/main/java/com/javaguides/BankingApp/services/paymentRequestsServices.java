package com.javaguides.BankingApp.services;

import com.javaguides.BankingApp.requests.paymentRequestDTO;
import com.javaguides.BankingApp.requests.paymentRequests;
import com.javaguides.BankingApp.requests.paymentRequestsRepository;
import com.javaguides.BankingApp.requests.requestStatus;
import com.javaguides.BankingApp.user.User;
import com.javaguides.BankingApp.wallet.Wallet;
import com.javaguides.BankingApp.wallet.WalletRepository;
import com.javaguides.BankingApp.wallet.WalletType;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
public class paymentRequestsServices {

    @Autowired
    private paymentRequestsRepository paymentRequestsRepository;

    @Autowired
    private emailService emailService;

    @Autowired
    private transactionServices transactionServices;

    @Autowired
    private WalletRepository walletRepository;

    public void requestPayment(User requester, User requestedUser, double amount) {
        paymentRequests request = new paymentRequests();
        request.setStatus(requestStatus.PENDING);
        request.setRequester(requester);
        request.setRequestedUser(requestedUser);
        request.setAmount(amount);
        request.setTime(LocalDateTime.now());

        paymentRequestsRepository.save(request);
        log.info("Payment request created: {} by requester: {} for amount: {}", request.getId(), requester.getId(), amount);

        emailService.sendPaymentRequestEmail(requestedUser.getEmail(), requestedUser.getFirstName(), requester.getFirstName(), amount);
        log.info("Payment request email sent to: {}", requestedUser.getEmail());
    }

    public List<paymentRequestDTO> getPendingRequests(User requestedUser) {
        List<paymentRequests> requests = paymentRequestsRepository.findByRequestedUserAndStatus(requestedUser, requestStatus.PENDING);
        log.info("Fetched {} pending requests for user: {}", requests.size(), requestedUser.getId());

        return requests.stream()
                .map(req -> new paymentRequestDTO(
                        req.getId(),                          // Include this line
                        req.getRequester().getFirstName(),
                        req.getAmount(),
                        req.getStatus(),
                        req.getTime()))
                .toList();
    }

    public void processRequest(int requestId, boolean accept, User requestedUser, WalletType walletType) {
        try {
            // Find the requested user's (user B) wallet based on the wallet type
            Wallet senderWallet = walletRepository.findByUserAndType(requestedUser, walletType)
                    .orElseThrow(() -> new IllegalArgumentException("Sender's wallet not found"));
            log.info("Sender wallet found for user: {} with type: {}", requestedUser.getId(), walletType);

            // Fetch the payment request by ID
            Optional<paymentRequests> optionalRequest = paymentRequestsRepository.findById(requestId);
            if (optionalRequest.isPresent()) {
                paymentRequests request = optionalRequest.get();

                // Check if the request belongs to the requested user (user B)
                if (!request.getRequestedUser().equals(requestedUser)) {
                    log.error("Request ID: {} does not belong to the user: {}", requestId, requestedUser.getId());
                    throw new IllegalArgumentException("Request does not belong to the user");
                }

                if (accept) {
                    // If accepted, update request status to ACCEPTED
                    request.setStatus(requestStatus.ACCEPTED);
                    paymentRequestsRepository.save(request);
                    log.info("Payment request ID: {} accepted by user: {}", requestId, requestedUser.getId());

                    // Find the requester’s (user A) wallet of the same wallet type
                    Wallet receiverWallet = walletRepository.findByUserAndType(request.getRequester(), walletType)
                            .orElseThrow(() -> new IllegalArgumentException("Requester wallet not found"));

                    // Call the sendMoney method to transfer the amount from user B to user A
                    transactionServices.sendMoney(requestedUser, receiverWallet.getIban(), request.getAmount(), walletType);
                    log.info("Transferred {} from user: {} to user: {} via wallet type: {}", request.getAmount(), requestedUser.getId(), request.getRequester().getId(), walletType);

                } else {
                    // If rejected, update request status to REJECTED and notify the requester
                    request.setStatus(requestStatus.REJECTED);
                    paymentRequestsRepository.save(request);
                    log.info("Payment request ID: {} rejected by user: {}", requestId, requestedUser.getId());

                    // Notify user A (requester) that the payment request was rejected
                    emailService.sendPaymentRejectedNotification(request.getRequester().getEmail(),
                            request.getRequester().getFirstName(), requestedUser.getFirstName(), request.getAmount());
                    log.info("Payment rejection email sent to: {}", request.getRequester().getEmail());
                }
            } else {
                log.error("Payment request ID: {} not found", requestId);
                throw new IllegalArgumentException("Payment request not found");
            }
        } catch (Exception e) {
            log.error("Error processing payment request ID: {}", requestId, e);
            throw e; // Re-throw the exception after logging
        }
    }
}
