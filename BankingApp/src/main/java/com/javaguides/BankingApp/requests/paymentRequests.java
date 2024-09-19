package com.javaguides.BankingApp.requests;

import com.javaguides.BankingApp.user.User;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "payment_request")
public class paymentRequests {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    @JoinColumn(name = "requester_id", referencedColumnName = "id")
    private User requester;

    @ManyToOne
    @JoinColumn(name = "requested_user_id", referencedColumnName = "id")
    private User requestedUser;

    private double amount;

    @Enumerated(EnumType.STRING)
    private requestStatus status;

    private LocalDateTime time;
}
