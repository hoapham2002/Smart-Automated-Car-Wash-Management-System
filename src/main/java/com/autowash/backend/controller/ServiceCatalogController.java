package com.autowash.backend.controller;

import com.autowash.backend.common.response.ApiResponse;
import com.autowash.backend.dto.response.ServicePriceResponse;
import com.autowash.backend.dto.response.ServiceResponse;
import com.autowash.backend.enums.VehicleSize;
import com.autowash.backend.service.ServiceCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** A07: public service catalog + pricing. */
@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@Tag(name = "Service Catalog", description = "Wash services and per-vehicle-size pricing")
public class ServiceCatalogController {

    private final ServiceCatalogService serviceCatalogService;

    @GetMapping
    @Operation(summary = "Danh sách dịch vụ + giá (theo vehicle_size nếu có)")
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getServices(
            @RequestParam(required = false) VehicleSize vehicleSize) {
        return ResponseEntity.ok(ApiResponse.success(serviceCatalogService.getServices(vehicleSize)));
    }

    @GetMapping("/{id}/prices")
    @Operation(summary = "Giá của 1 dịch vụ theo từng loại xe")
    public ResponseEntity<ApiResponse<List<ServicePriceResponse>>> getPrices(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(serviceCatalogService.getPrices(id)));
    }
}
