package com.autowash.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Maps to table `services` - the wash service CATALOG (e.g. "Rửa tiêu chuẩn").
 *
 * NAMING COLLISION NOTICE: this class is deliberately named `WashService`,
 * matching the original project file tree (entity/WashService.java), even
 * though `com.autowash.service.WashService` (the Wash Process business
 * logic interface, Week 3) has the exact same simple name in a different
 * package. Any file that needs BOTH must reference this one by its fully
 * qualified name `com.autowash.entity.WashService` instead of importing it -
 * see RedemptionOption.java for the established pattern.
 */
@Entity
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WashService {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 120)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_min", nullable = false)
    @Builder.Default
    private Integer durationMin = 15;

    @Column(name = "base_points", nullable = false)
    @Builder.Default
    private Integer basePoints = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
