package com.autowash.autowash_pro.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; 
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autowash.autowash_pro.entity.Promotion;
import com.autowash.autowash_pro.dto.request.promotion.PromotionRequest;
import com.autowash.autowash_pro.service.PromotionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotion Admin", description = "Quản lý khuyến mãi dành cho Admin")
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    @Operation(summary = "Lấy danh sách khuyến mãi với bộ lọc trạng thái và tìm kiếm")
    public ResponseEntity<List<Promotion>> getAdminPromotions(
            @RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
            @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(promotionService.getPromotionsByStatus(status, search));
    }

    @PostMapping
    @Operation(summary = "Tạo mới khuyến mãi")
    public ResponseEntity<Promotion> createPromotion(@RequestBody PromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(promotionService.createPromotion(request));
    }
    
    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Bật/tắt trạng thái hoạt động của khuyến mãi")
    public ResponseEntity<Promotion> togglePromotionStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(promotionService.togglePromotionStatus(id));
    }

    @PostMapping("/{id}/send")
    @Operation(summary = "Gửi thông báo khuyến mãi đến các đối tượng mục tiêu")
    public ResponseEntity<String> sendPromotionToTargets(@PathVariable UUID id) {
        promotionService.sendPromotionToTargetUsers(id);
        return ResponseEntity.ok("Chiến dịch gửi thông báo khuyến mãi đã được kích hoạt thành công!");
    }
}