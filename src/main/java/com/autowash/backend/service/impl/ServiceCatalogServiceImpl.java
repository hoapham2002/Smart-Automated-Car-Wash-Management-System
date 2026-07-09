package com.autowash.backend.service.impl;

import com.autowash.backend.dto.response.ServicePriceResponse;
import com.autowash.backend.dto.response.ServiceResponse;
import com.autowash.backend.entity.WashService;
import com.autowash.backend.enums.VehicleSize;
import com.autowash.backend.exception.BusinessException;
import com.autowash.backend.exception.ErrorCode;
import com.autowash.backend.repository.ServicePriceRepository;
import com.autowash.backend.repository.WashServiceRepository;
import com.autowash.backend.service.ServiceCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** A07: read-only service catalog + pricing. */
@Service
@RequiredArgsConstructor
public class ServiceCatalogServiceImpl implements ServiceCatalogService {

    private final WashServiceRepository washServiceRepository;
    private final ServicePriceRepository servicePriceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> getServices(VehicleSize vehicleSize) {
        return washServiceRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(svc -> {
                    var price = vehicleSize != null
                            ? servicePriceRepository.findByServiceIdAndVehicleSize(svc.getId(), vehicleSize).orElse(null)
                            : null;
                    return ServiceResponse.builder()
                            .id(svc.getId())
                            .name(svc.getName())
                            .description(svc.getDescription())
                            .durationMin(svc.getDurationMin())
                            .basePoints(svc.getBasePoints())
                            .price(price != null ? price.getPrice() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicePriceResponse> getPrices(UUID serviceId) {
        WashService service = washServiceRepository.findById(serviceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SERVICE_NOT_FOUND, "Không tìm thấy dịch vụ"));

        return servicePriceRepository.findByServiceId(service.getId()).stream()
                .map(p -> ServicePriceResponse.builder()
                        .vehicleSize(p.getVehicleSize())
                        .price(p.getPrice())
                        .build())
                .collect(Collectors.toList());
    }
}
