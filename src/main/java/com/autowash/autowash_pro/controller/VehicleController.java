package com.autowash.autowash_pro.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autowash.autowash_pro.dto.request.vehicle.VehicleRequest;
import com.autowash.autowash_pro.dto.response.vehicle.VehicleResponse;
import com.autowash.autowash_pro.service.VehicleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Quan ly xe cua khach hang")
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    @Operation(summary = "Lay danh sach xe cua tai khoan dang nhap")
    public ResponseEntity<List<VehicleResponse>> getMyVehicles(
            Authentication authentication) {
        return ResponseEntity.ok(
            vehicleService.getMyVehicles(authentication.getName()));
    }

    @PostMapping
    @Operation(summary = "Them xe moi cho tai khoan dang nhap")
    public ResponseEntity<VehicleResponse> createVehicle(
            Authentication authentication,
            @RequestBody @Valid VehicleRequest request) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(vehicleService.createVehicle(
                authentication.getName(), request));
    }

    @PutMapping("/{vehicleId}")
    @Operation(summary = "Cap nhat thong tin xe")
    public ResponseEntity<VehicleResponse> updateVehicle(
            Authentication authentication,
            @PathVariable UUID vehicleId,
            @RequestBody @Valid VehicleRequest request) {
        return ResponseEntity.ok(vehicleService.updateVehicle(
            authentication.getName(), vehicleId, request));
    }

    @PatchMapping("/{vehicleId}/primary")
    @Operation(summary = "Dat xe mac dinh")
    public ResponseEntity<VehicleResponse> setPrimaryVehicle(
            Authentication authentication,
            @PathVariable UUID vehicleId) {
        return ResponseEntity.ok(vehicleService.setPrimaryVehicle(
            authentication.getName(), vehicleId));
    }

    @DeleteMapping("/{vehicleId}")
    @Operation(summary = "Xoa xe")
    public ResponseEntity<Void> deleteVehicle(
            Authentication authentication,
            @PathVariable UUID vehicleId) {
        vehicleService.deleteVehicle(authentication.getName(), vehicleId);
        return ResponseEntity.noContent().build();
    }
}
