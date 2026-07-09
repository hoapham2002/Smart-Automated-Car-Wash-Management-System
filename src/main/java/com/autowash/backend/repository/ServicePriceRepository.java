package com.autowash.backend.repository;

import com.autowash.backend.entity.ServicePrice;
import com.autowash.backend.enums.VehicleSize;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServicePriceRepository extends JpaRepository<ServicePrice, UUID> {

    /** A09/A10 - price lookup at booking time (service + the vehicle's own size). */
    Optional<ServicePrice> findByServiceIdAndVehicleSize(UUID serviceId, VehicleSize vehicleSize);

    /** A07 - GET /services/:id/prices (every vehicle size for one service). */
    List<ServicePrice> findByServiceId(UUID serviceId);
}
