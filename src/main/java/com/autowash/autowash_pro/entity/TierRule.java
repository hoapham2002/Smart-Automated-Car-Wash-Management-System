package com.autowash.autowash_pro.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tier_rules")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String tier;

    @Column(nullable = false, length = 50)
    private String label;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int threshold;

    @Column(name = "booking_window", nullable = false)
    private int bookingWindow;

    @Column(nullable = false)
    private int multiplier;

    @Column(columnDefinition = "TEXT")
    private String perks;

    @Column(name = "class_name", length = 100)
    private String className;
}
