package com.autowash.backend.entity;

import com.autowash.backend.enums.VehicleSize;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Maps to table `vehicles`.
 */
@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "plate_number", nullable = false, length = 20)
    private String plateNumber;

    /** Normalized via PlateNormalizer before persisting - see uq_vehicle_plate. */
    @Column(name = "plate_normalized", nullable = false, unique = true, length = 20)
    private String plateNormalized;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "vehicle_size", nullable = false, columnDefinition = "vehicle_size")
    @Builder.Default
    private VehicleSize vehicleSize = VehicleSize.MOTORBIKE;

    @Column(name = "brand", length = 60)
    private String brand;

    @Column(name = "model", length = 60)
    private String model;

    @Column(name = "color", length = 40)
    private String color;

    @Column(name = "year")
    private Short year;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private boolean primary = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
