package com.autowash.backend.service.impl;

import com.autowash.backend.common.util.PlateNormalizer;
import com.autowash.backend.dto.request.CreateVehicleRequest;
import com.autowash.backend.dto.request.UpdateVehicleRequest;
import com.autowash.backend.dto.response.VehicleResponse;
import com.autowash.backend.entity.User;
import com.autowash.backend.entity.Vehicle;
import com.autowash.backend.exception.BusinessException;
import com.autowash.backend.exception.ErrorCode;
import com.autowash.backend.repository.UserRepository;
import com.autowash.backend.repository.VehicleRepository;
import com.autowash.backend.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A05-A06: customer vehicle CRUD.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getMyVehicles(UUID ownerId) {
        return vehicleRepository.findByOwnerIdAndActiveTrueOrderByPrimaryDesc(ownerId).stream()
                .map(VehicleResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VehicleResponse create(UUID ownerId, CreateVehicleRequest request) {
        String normalized = PlateNormalizer.normalize(request.getPlateNumber());
        if (vehicleRepository.existsByPlateNormalized(normalized)) {
            throw new BusinessException(ErrorCode.PLATE_ALREADY_EXISTS, "Biển số này đã được đăng ký");
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng"));

        Vehicle vehicle = Vehicle.builder()
                .owner(owner)
                .plateNumber(request.getPlateNumber())
                .plateNormalized(normalized)
                .vehicleSize(request.getVehicleSize())
                .brand(request.getBrand())
                .model(request.getModel())
                .color(request.getColor())
                .primary(request.isPrimary())
                .build();
        vehicleRepository.save(vehicle);

        log.info("Vehicle {} ({}) created for owner {}", vehicle.getId(), normalized, ownerId);
        return VehicleResponse.from(vehicle);
    }

    @Override
    @Transactional
    public VehicleResponse update(UUID ownerId, UUID vehicleId, UpdateVehicleRequest request) {
        Vehicle vehicle = getOwnedVehicleOrThrow(vehicleId, ownerId);

        if (request.getPlateNumber() != null) {
            String normalized = PlateNormalizer.normalize(request.getPlateNumber());
            if (!normalized.equals(vehicle.getPlateNormalized())
                    && vehicleRepository.existsByPlateNormalized(normalized)) {
                throw new BusinessException(ErrorCode.PLATE_ALREADY_EXISTS, "Biển số này đã được đăng ký");
            }
            vehicle.setPlateNumber(request.getPlateNumber());
            vehicle.setPlateNormalized(normalized);
        }
        if (request.getBrand() != null) {
            vehicle.setBrand(request.getBrand());
        }
        if (request.getModel() != null) {
            vehicle.setModel(request.getModel());
        }
        if (request.getColor() != null) {
            vehicle.setColor(request.getColor());
        }
        if (request.getIsPrimary() != null) {
            vehicle.setPrimary(request.getIsPrimary());
        }

        vehicleRepository.save(vehicle);
        return VehicleResponse.from(vehicle);
    }

    @Override
    @Transactional
    public void delete(UUID ownerId, UUID vehicleId) {
        Vehicle vehicle = getOwnedVehicleOrThrow(vehicleId, ownerId);
        vehicle.setActive(false);
        vehicleRepository.save(vehicle);
        log.info("Vehicle {} soft-deleted by owner {}", vehicleId, ownerId);
    }

    private Vehicle getOwnedVehicleOrThrow(UUID vehicleId, UUID ownerId) {
        return vehicleRepository.findByIdAndOwnerId(vehicleId, ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VEHICLE_NOT_FOUND, "Không tìm thấy xe"));
    }
}
