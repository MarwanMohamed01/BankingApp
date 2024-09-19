package com.javaguides.BankingApp.services;

import com.javaguides.BankingApp.wallet.WalletType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.Semaphore;

@Log4j2
@Service
@RequiredArgsConstructor
public class emailService {

    private final JavaMailSender javaMailSender;
    private final Semaphore semaphore = new Semaphore(10); // Adjust the number based on the provider's limits

    @Async
    public void sendRegistrationEmail(String to, String username) {
        log.info("Sending registration email to {}", to);
        sendEmailWithRetry(createRegistrationMessage(to, username));
    }

    @Async
    public void sendDepositNotification(String to, String username, double amount, WalletType walletType) {
        log.info("Sending deposit notification to {}", to);
        sendEmailWithRetry(createDepositNotificationMessage(to, username, amount, walletType));
    }

    @Async
    public void sendTransferNotification(String to, String senderUsername, double amount, WalletType walletType, String receiverName) {
        log.info("Sending transfer notification to {}", to);
        sendEmailWithRetry(createTransferNotificationMessage(to, senderUsername, amount, walletType, receiverName));
    }

    @Async
    public void receiveTransferNotification(String to, String senderUsername, double amount, WalletType walletType, String receiverName) {
        log.info("Sending receive transfer notification to {}", to);
        sendEmailWithRetry(createReceiveTransferNotificationMessage(to, senderUsername, amount, walletType, receiverName));
    }

    @Async
    public void sendVerificationNotification(String to, String firstName) {
        log.info("Sending verification notification to {}", to);
        sendEmailWithRetry(createVerificationNotificationMessage(to, firstName));
    }

    @Async
    public void sendPaymentRequestEmail(String to, String username, String requester, double amount) {
        log.info("Sending payment request email to {}", to);
        sendEmailWithRetry(createPaymentRequestMessage(to, username, requester, amount));
    }

    @Async
    public void sendPaymentRejectedNotification(String to, String username, String requestedName, double amount) {
        log.info("Sending payment rejected notification to {}", to);
        sendEmailWithRetry(createPaymentRejectedMessage(to, username, requestedName, amount));
    }

    private SimpleMailMessage createRegistrationMessage(String to, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("abginternship1@outlook.com");
        message.setTo(to);
        message.setSubject("Registration Successful");
        message.setText("Hello " + username + ",\n\n" +
                "You have successfully registered in our banking app. Welcome aboard!\n\n" +
                "Best regards,\n" +
                "The Banking App Team");
        return message;
    }

    private SimpleMailMessage createDepositNotificationMessage(String to, String username, double amount, WalletType walletType) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("abginternship1@outlook.com");
        message.setTo(to);
        message.setSubject("Deposit Successful");
        message.setText("Hello " + username + ",\n\n" +
                "You have successfully deposited " + amount + " into your " + walletType + " wallet.\n\n" +
                "Best regards,\n" +
                "The Banking App Team");
        return message;
    }

    private SimpleMailMessage createTransferNotificationMessage(String to, String senderUsername, double amount, WalletType walletType, String receiverName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("abginternship1@outlook.com");
        message.setTo(to);
        message.setSubject("Money Transferred");
        message.setText("Hello " + senderUsername + ",\n\n" +
                "You have successfully transferred " + amount + " from your " + walletType + " wallet to " + receiverName + ".\n\n" +
                "Best regards,\n" +
                "The Banking App Team");
        return message;
    }

    private SimpleMailMessage createReceiveTransferNotificationMessage(String to, String senderUsername, double amount, WalletType walletType, String receiverName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("abginternship1@outlook.com");
        message.setTo(to);
        message.setSubject("Money Received");
        message.setText("Hello " + receiverName + ",\n\n" +
                "You have successfully received " + amount + " to your " + walletType + " wallet from " + senderUsername + ".\n\n" +
                "Best regards,\n" +
                "The Banking App Team");
        return message;
    }

    private SimpleMailMessage createVerificationNotificationMessage(String to, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("abginternship1@outlook.com");
        message.setTo(to);
        message.setSubject("Account Verification Completed");
        message.setText("Dear " + firstName + ",\n\n" +
                "We are pleased to inform you that your account has been successfully verified.\n\n" +
                "Best regards,\n" +
                "The Banking App Team");
        return message;
    }

    private SimpleMailMessage createPaymentRequestMessage(String to, String username, String requester, double amount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("abginternship1@outlook.com");
        message.setTo(to);
        message.setSubject("Payment Request from " + requester);
        message.setText("Hello " + username + ",\n\n" +
                requester + " has requested a payment of " + amount + " from you.\n\n" +
                "Please log in to your account to accept or reject the request.\n\n" +
                "Thank you,\n" +
                "Your Banking App Team");
        return message;
    }

    private SimpleMailMessage createPaymentRejectedMessage(String to, String username, String requestedName, double amount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("abginternship1@outlook.com");
        message.setTo(to);
        message.setSubject("Payment Request Rejected");
        message.setText("Hello " + username + ",\n\n" +
                "Unfortunately, your payment request of " + amount + " has been rejected by " + requestedName + ".\n\n" +
                "Please feel free to contact the user if needed or try again later.\n\n" +
                "Best regards,\n" +
                "Your Banking App Team");
        return message;
    }

    private void sendEmailWithRetry(SimpleMailMessage message) {
        int retries = 5;
        int backoff = 1000; // Initial backoff time in milliseconds

        while (retries > 0) {
            try {
                semaphore.acquire(); // Acquire permit
                javaMailSender.send(message);
                log.info("Email sent successfully to {}", message.getTo());
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Error acquiring semaphore: {}", e.getMessage(), e);
            } catch (Exception e) {
                retries--;
                if (retries == 0) {
                    log.error("Failed to send email after retries: {}", e.getMessage(), e);
                } else {
                    log.warn("Failed to send email. Retrying... {} attempts left", retries);
                    try {
                        Thread.sleep(backoff); // Wait before retrying
                        backoff *= 2; // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Error during backoff sleep: {}", ie.getMessage(), ie);
                    }
                }
            } finally {
                semaphore.release(); // Release permit
            }
        }
    }
}
