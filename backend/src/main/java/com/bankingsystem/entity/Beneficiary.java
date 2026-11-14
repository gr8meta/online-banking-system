package com.bankingsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "beneficiaries", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"customer_id", "account_number"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Beneficiary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long beneficiaryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, length = 100)
    private String beneficiaryName;

    @Column(nullable = false, length = 20)
    private String accountNumber;

    @Column(nullable = false, length = 15)
    private String ifscCode;

    @Column(nullable = false, length = 100)
    private String bankName;

    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum Status { ACTIVE, INACTIVE }
}
