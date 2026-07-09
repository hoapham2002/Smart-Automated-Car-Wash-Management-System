package com.autowash.backend.service;

import com.autowash.backend.dto.response.ServicePriceResponse;
import com.autowash.backend.dto.response.ServiceResponse;
import com.autowash.backend.enums.VehicleSize;

import java.util.List;
import java.util.UUID;

/** Backs A07: service catalog + per-vehicle-size pricing. */
public interface ServiceCatalogService {

    /** GET /services?vehicle_size= */
    List<ServiceResponse> getServices(VehicleSize vehicleSize);

    /** GET /services/:id/prices */
    List<ServicePriceResponse> getPrices(UUID serviceId);
}
