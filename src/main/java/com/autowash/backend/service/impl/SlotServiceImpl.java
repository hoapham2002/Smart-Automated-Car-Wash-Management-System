package com.autowash.backend.service.impl;

import com.autowash.backend.dto.response.SlotResponse;
import com.autowash.backend.entity.BookingSlot;
import com.autowash.backend.exception.BusinessException;
import com.autowash.backend.repository.BookingSlotRepository;
import com.autowash.backend.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** A08: available slots grouped by day. */
@Service
@RequiredArgsConstructor
public class SlotServiceImpl implements SlotService {

    private static final int MAX_RANGE_DAYS = 60;

    private final BookingSlotRepository bookingSlotRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<LocalDate, List<SlotResponse>> getSlots(LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) {
            throw new BusinessException("INVALID_DATE_RANGE", "Khoảng thời gian 'from'/'to' không hợp lệ");
        }
        if (java.time.temporal.ChronoUnit.DAYS.between(from, to) > MAX_RANGE_DAYS) {
            throw new BusinessException("DATE_RANGE_TOO_LARGE",
                    "Khoảng thời gian tối đa là " + MAX_RANGE_DAYS + " ngày");
        }

        List<BookingSlot> slots = bookingSlotRepository
                .findBySlotDateBetweenAndBlockedFalseOrderBySlotDateAscSlotStartAsc(from, to);

        Map<LocalDate, List<SlotResponse>> grouped = new LinkedHashMap<>();
        for (BookingSlot slot : slots) {
            grouped.computeIfAbsent(slot.getSlotDate(), d -> new java.util.ArrayList<>())
                    .add(SlotResponse.builder()
                            .id(slot.getId())
                            .slotStart(slot.getSlotStart())
                            .slotEnd(slot.getSlotEnd())
                            .remaining(slot.getRemaining())
                            .build());
        }
        return grouped;
    }
}
