package com.autowash.autowash_pro.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rewards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 50)
    private String points;

    @Column(name = "minimum_tier", nullable = false, length = 50)
    private String minimumTier;

    @Column(name = "tier_class_name", nullable = false, length = 100)
    private String tierClassName;
}
