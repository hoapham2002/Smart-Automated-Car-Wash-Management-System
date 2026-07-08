package com.autowash.backend.service;

import com.autowash.backend.dto.request.CreateVehicleRequest;
import com.autowash.backend.dto.request.UpdateVehicleRequest;
import com.autowash.backend.dto.response.VehicleResponse;

import java.util.List;
import java.util.UUID;

/**
 * Backs A05-A06: list/create/update/delete a customer's own vehicles.
 */
public interface VehicleService {

    /** A05 - GET /me/vehicles */
    List<VehicleResponse> getMyVehicles(UUID ownerId);

    /** A05 - POST /me/vehicles */
    VehicleResponse create(UUID ownerId, CreateVehicleRequest request);

    /** A06 - PUT /me/vehicles/:id */
    VehicleResponse update(UUID ownerId, UUID vehicleId, UpdateVehicleRequest request);

    /** A06 - DELETE /me/vehicles/:id (soft delete via is_active) */
    void delete(UUID ownerId, UUID vehicleId);
}
