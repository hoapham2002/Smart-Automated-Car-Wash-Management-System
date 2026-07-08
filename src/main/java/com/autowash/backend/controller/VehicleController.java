package com.autowash.backend.controller;

import com.autowash.backend.common.response.ApiResponse;
import com.autowash.backend.dto.request.CreateVehicleRequest;
import com.autowash.backend.dto.request.UpdateVehicleRequest;
import com.autowash.backend.dto.response.VehicleResponse;
import com.autowash.backend.security.SecurityUtils;
import com.autowash.backend.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * A05-A06: a customer's own vehicles.
 */
@RestController
@RequestMapping("/me/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicle", description = "Customer's own vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    @Operation(summary = "Danh sách xe của khách hàng hiện tại")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getMyVehicles() {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.getMyVehicles(SecurityUtils.currentUserId())));
    }

    @PostMapping
    @Operation(summary = "Thêm xe mới")
    public ResponseEntity<ApiResponse<VehicleResponse>> create(@Valid @RequestBody CreateVehicleRequest request) {
        var created = vehicleService.create(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Sửa thông tin xe")
    public ResponseEntity<ApiResponse<VehicleResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateVehicleRequest request) {
        var updated = vehicleService.update(SecurityUtils.currentUserId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xoá xe (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        vehicleService.delete(SecurityUtils.currentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
