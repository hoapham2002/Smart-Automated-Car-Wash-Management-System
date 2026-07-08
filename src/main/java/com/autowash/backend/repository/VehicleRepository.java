package com.autowash.backend.repository;

import com.autowash.backend.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    /** A05 - GET /me/vehicles */
    List<Vehicle> findByOwnerIdAndActiveTrueOrderByPrimaryDesc(UUID ownerId);

    /**
     * A06/A09 - ownership check used both when editing/deleting a vehicle
     * and (Week 2) when creating a booking, so a customer can never
     * act on a vehicle that isn't theirs.
     */
    Optional<Vehicle> findByIdAndOwnerId(UUID id, UUID ownerId);

    boolean existsByPlateNormalized(String plateNormalized);

    /**
     * A10 - fuzzy plate search for walk-in bookings (staff typing a plate
     * that might have typos/missing dashes). Relies on the pg_trgm extension
     * + idx_vehicles_plate GIN index already defined in the schema.
     */
    @org.springframework.data.jpa.repository.Query(value = """
            SELECT * FROM vehicles
            WHERE plate_normalized % :plateNormalized
            ORDER BY similarity(plate_normalized, :plateNormalized) DESC
            LIMIT 5
            """, nativeQuery = true)
    List<Vehicle> fuzzySearchByPlate(String plateNormalized);
}
