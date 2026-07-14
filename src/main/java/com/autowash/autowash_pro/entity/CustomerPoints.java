package com.autowash.autowash_pro.entity;


import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.autowash.autowash_pro.enums.PointType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer_points")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "point_id", updatable = false, nullable = false)
    private UUID pointId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointType type;

    @Column(nullable = false)
    private int points;

    @Column(name = "balance_after", nullable = false)
    private int balanceAfter;

    // Tham chiếu đến wash_id hoặc booking_id
    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(length = 200)
    private String description;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}