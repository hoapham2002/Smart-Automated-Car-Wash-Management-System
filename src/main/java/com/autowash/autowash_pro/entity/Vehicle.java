package com.autowash.autowash_pro.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "vehicles")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 🌟 ĐÃ THÊM: Ngắt bỏ lớp bọc Hibernate Proxy khi thực hiện bốc tách và parse luồng stream map dữ liệu danh sách xe
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "vehicle_id", updatable = false, nullable = false)
    private UUID vehicleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"vehicles", "bookings", "points"}) // Ngăn chặn vòng lặp vô hạn tuần hoàn JSON khi liên kết ngược về chủ xe
    private Customer customer;

    @Column(name = "license_plate",
            unique = true, nullable = false, length = 20)
    private String licensePlate;

    @Column(name = "vehicle_type", nullable = false, length = 50)
    private String vehicleType;

    @Column(length = 50)
    private String brand;

    @Column(length = 30)
    private String color;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private boolean isPrimary = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}