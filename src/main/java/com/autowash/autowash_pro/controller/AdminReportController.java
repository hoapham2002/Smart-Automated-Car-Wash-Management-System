package com.autowash.autowash_pro.controller;

import com.autowash.autowash_pro.service.AdminReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Reports & Analytics", description = "Quản lý Báo cáo & Thống kê")
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping("/revenue")
    @Operation(summary = "Báo cáo doanh thu nâng cao")
    public ResponseEntity<Map<String, Object>> getRevenueReport(
            @RequestParam(value = "granularity", defaultValue = "day") String granularity,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        return ResponseEntity.ok(adminReportService.getRevenueReport(granularity, startDate, endDate));
    }

    @GetMapping("/customers")
    @Operation(summary = "Báo cáo khách hàng & Tăng trưởng")
    public ResponseEntity<Map<String, Object>> getCustomerReport(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        return ResponseEntity.ok(adminReportService.getCustomerReport(startDate, endDate));
    }
}
