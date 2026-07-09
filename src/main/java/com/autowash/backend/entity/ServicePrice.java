package com.autowash.backend.entity;

import com.autowash.backend.enums.VehicleSize;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Maps to table `service_prices` - price of one service, per vehicle size.
 */
@Entity
@Table(name = "service_prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePrice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private WashService service; // com.autowash.entity.WashService (same package, no clash here)

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "vehicle_size", nullable = false, columnDefinition = "vehicle_size")
    private VehicleSize vehicleSize;

    @Column(name = "price", nullable = false, precision = 12, scale = 0)
    private BigDecimal price;
}
