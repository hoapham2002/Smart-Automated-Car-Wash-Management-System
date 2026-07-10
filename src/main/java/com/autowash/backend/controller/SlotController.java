package com.autowash.backend.controller;

import com.autowash.backend.common.response.ApiResponse;
import com.autowash.backend.dto.response.SlotResponse;
import com.autowash.backend.service.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** A08: public booking slot availability, grouped by day. */
@RestController
@RequestMapping("/slots")
@RequiredArgsConstructor
@Tag(name = "Slots", description = "Available booking slots, grouped by day")
public class SlotController {

    private final SlotService slotService;

    @GetMapping
    @Operation(summary = "Slot còn trống trong khoảng from-to, nhóm theo ngày")
    public ResponseEntity<ApiResponse<Map<LocalDate, List<SlotResponse>>>> getSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(slotService.getSlots(from, to)));
    }
}
