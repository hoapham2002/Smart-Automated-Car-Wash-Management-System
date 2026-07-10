package com.autowash.backend.service;

import com.autowash.backend.dto.response.SlotResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Backs A08: available booking slots grouped by day. */
public interface SlotService {

    /** GET /slots?from=&to= - keys are ISO dates, matching the API doc's {"2024-07-10": [...]} shape. */
    Map<LocalDate, List<SlotResponse>> getSlots(LocalDate from, LocalDate to);
}
